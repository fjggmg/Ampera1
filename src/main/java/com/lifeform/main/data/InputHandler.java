package com.lifeform.main.data;

import com.lifeform.main.IKi;
import com.lifeform.main.Settings;
import com.lifeform.main.StringSettings;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.blockchain.BlockState;
import com.lifeform.main.blockchain.ChainManager;
import com.lifeform.main.blockchain.GPUMiner;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.network.packets.Ping;
import com.lifeform.main.network.packets.TransactionDataRequest;
import com.lifeform.main.transactions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class InputHandler extends Thread {

    private IKi ki;

    public InputHandler(IKi ki) {
        setDaemon(true);
        this.ki = ki;
    }

    DecimalFormat format = new DecimalFormat("###,###,###,###,###,###,###");
    @Override
    public void run() {
        setName("CommandLine Input");
        BufferedReader s = null;
        try {
            s = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            ki.getMainLog().error("Unable to create input reader for handling CLI. ", e);
            return;
        }
        while (true) {
            try {
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                String line = s.readLine();

                if (line.startsWith("exit")) {
                    System.out.println("Exiting program");
                    ki.close();
                    //System.exit(0);
                } else if (line.startsWith("mineGPU")) {
                    System.out.println("starting gpu miner");
                    ki.getMinerMan().startMiners();
                } else if (line.startsWith("checkConnections")) {

                    ki.getMainLog().info("Connection List Status: ");
                    ki.getMainLog().info("Number of Conns: " + ki.getNetMan().getConnections().size());
                    int i = 0;
                    for (IConnectionManager conn : ki.getNetMan().getConnections()) {
                        Ping ping = new Ping();
                        ping.currentTime = System.currentTimeMillis();
                        long currentTime = System.currentTimeMillis();
                        conn.sendPacket(ping);
                        ki.getMainLog().info("Connection #" + i);
                        ki.getMainLog().info("Status: " + ((conn.isConnected()) ? "Connected" : "Disconnected"));
                        if (conn.isConnected()) {
                            long uptime = conn.uptime();
                            long days = uptime / 86400000;
                            uptime -= days * 8640000;
                            long hours = uptime / 3600000;
                            uptime -= hours * 3600000;
                            long minutes = uptime / 60000;
                            uptime -= minutes * 60000;
                            long seconds = uptime / 1000;
                            uptime -= seconds * 1000;
                            long milliseconds = uptime;

                            ki.getMainLog().info("Uptime: " + days + "D " + hours + "H " + minutes + "M " + seconds + "S " + milliseconds + "ms");
                            ki.getNetMan().setLive(false);


                            boolean over5k = false;
                            while (!ki.getNetMan().live()) {
                                try {
                                    sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (System.currentTimeMillis() > currentTime + 5000) {
                                    over5k = true;
                                    break;
                                }
                            }
                            if (!over5k)
                                ki.getMainLog().info("Current latency: " + (System.currentTimeMillis() - currentTime));
                            else
                                ki.getMainLog().info("Current latency is over 5,000");
                        }
                        i++;
                    }
                } else if (line.startsWith("sendTransaction")) {
                    ki.getMainLog().info("Not implemented for NovusTX system yet");
                } else if (line.startsWith("relays")) {
                    ki.getMainLog().info("Relay list: ");
                    for (String relay : ki.getNetMan().getRelays()) {
                        ki.getMainLog().info(relay);
                    }
                } else if (line.startsWith("requestTransactions")) {
                    ki.getMainLog().info("Requesting transaction data for addresses:");
                    TransactionDataRequest tdr = new TransactionDataRequest();
                    for (IAddress a : ki.getAddMan().getAll()) {
                        ki.getMainLog().info(a.encodeForChain());
                    }
                    ki.getNetMan().broadcast(tdr);


                } else if (line.startsWith("blockData")) {
                    String[] args = line.split(" ");
                    if (args.length < 2) {
                        ki.debug("Not enough args");
                        continue;
                    }
                    ki.debug("========Block Data For #" + args[1] + "==============");
                    BigInteger height;
                    try {
                        height = new BigInteger(args[1]);
                    } catch (Exception e) {
                        continue;
                    }
                    Block b = ki.getChainMan().getByHeight(height);
                    if (b == null) {
                        ki.debug("Block is null");
                        continue;
                    }

                    ki.debug("Timestamp: " + new Date(ki.getChainMan().getByHeight(height).timestamp).toString());
                    ki.debug("Block header: " + b.header());
                    ki.debug("Solver: " + b.getCoinbase().getOutputs().get(0).getAddress().encodeForChain());
                    ki.debug("Block ID: " + b.ID);
                    ki.debug("Block hash: " + EncryptionManager.sha512(b.header()));
                    ki.debug("Transactions: " + b.getTransactionKeys().size());
                    ki.debug("TransactionIDs: ");
                    for (String key : b.getTransactionKeys()) {
                        ki.debug("ID: " + key);
                    }

                    ki.debug("Block PrevID: " + b.prevID);

                } else if (line.startsWith("setHeight")) {
                    String[] args = line.split(" ");
                    if (args.length < 2) {
                        ki.debug("Not enough args");
                        continue;
                    }
                    BigInteger height;
                    try {
                        height = new BigInteger(args[1]);
                    } catch (Exception e) {
                        continue;
                    }
                    if (height.compareTo(ki.getChainMan().currentHeight()) < 0) {
                        BigInteger h1 = new BigInteger(height.toByteArray());
                        for (; h1.compareTo(ki.getChainMan().currentHeight()) <= 0; h1 = h1.add(BigInteger.ONE)) {
                            for (String trans : ki.getChainMan().getByHeight(h1).getTransactionKeys()) {
                                ki.getTransMan().undoTransaction(ki.getChainMan().getByHeight(h1).getTransaction(trans));
                            }

                            ki.getTransMan().undoTransaction(ki.getChainMan().getByHeight(h1).getCoinbase());
                        }
                    }
                    ki.debug("Setting height to: " + height);
                    ki.getChainMan().setHeight(height);


                } else if (line.startsWith("allAdds")) {
                    for (IAddress a : ki.getAddMan().getAll()) {
                        ki.debug("Address: " + a.encodeForChain());
                    }

                } else if (line.startsWith("getUTXOs")) {
                    if (line.split(" ").length < 1) {
                        ki.debug("Not enough args");
                        continue;
                    }
                    try {
                        if (ki.getTransMan().getUTXOs(Address.decodeFromChain(line.split(" ")[1]), true) != null)
                            for (Output out : ki.getTransMan().getUTXOs(Address.decodeFromChain(line.split(" ")[1]), true)) {
                                ki.debug("Output data : " + out.getID() + " Address: " + out.getAddress().encodeForChain() + " Amount: " + out.getAmount());
                            }
                        ki.debug("Done getting UTXOs");
                    } catch (Exception e) {
                        ki.debug("Invalid address");
                    }


                } else if (line.startsWith("getBalance")) {
                    if (line.split(" ").length < 1) {
                        ki.debug("Not enough args");
                        continue;
                    }
                    if (ki.getTransMan().getUTXOs(Address.decodeFromChain(line.split(" ")[1]), true) != null)
                        ki.getMainLog().info("Balance in wallet is: " + ki.getTransMan().getAmountInWallet(Address.decodeFromChain(line.split(" ")[1]), Token.ORIGIN));


                } else if (line.startsWith("verifyTransactions")) {

                    BigInteger height = BigInteger.ZERO;
                    Set<String> inputs = new HashSet<>();
                    boolean success = true;
                    ML:
                    while (height.compareTo(ki.getChainMan().currentHeight()) <= 0) {
                        ki.debug("Verifying in block: " + height);
                        for (String trans : ki.getChainMan().getByHeight(height).getTransactionKeys()) {
                            for (Input i : ki.getChainMan().getByHeight(height).getTransaction(trans).getInputs()) {
                                if (inputs.contains(i.getID())) {
                                    ki.debug("Found double spend");
                                    success = false;
                                    break ML;
                                }
                                inputs.add(i.getID());
                            }
                        }
                        height = height.add(BigInteger.ONE);
                    }
                    ki.debug("Done testing result: " + ((success) ? "Success" : "Failure"));
                } else if (line.startsWith("rebuildChain")) {
                    BigInteger currentHeight = ki.getChainMan().currentHeight();
                    ki.getChainMan().setHeight(BigInteger.valueOf(-1));
                    ki.getTransMan().clear();
                    while (ki.getChainMan().currentHeight().compareTo(currentHeight) <= 0) {
                        ki.debug("Rebuilding block: " + currentHeight);
                        BlockState state = ki.getChainMan().addBlock(ki.getChainMan().getByHeight(currentHeight));
                        if (!state.success()) {
                            ki.getMainLog().fatal("Unable to rebuild chain, state at block: " + currentHeight + " is " + state);
                            return;
                        }
                        currentHeight = currentHeight.add(BigInteger.ONE);
                    }
                    ki.debug("Rebuilt chain successfully");

                } else if (line.startsWith("checkSolver")) {
                    if (line.split(" ").length < 2) {
                        ki.debug("Not enough arguments");
                        continue;
                    }
                    ki.debug("Starting to check for solver in blocks");
                    BigInteger height = BigInteger.ZERO;
                    int found = 0;
                    while (height.compareTo(ki.getChainMan().currentHeight()) <= 0) {
                        if (ki.getChainMan().getByHeight(height).getCoinbase().getOutputs().get(0).getAddress().encodeForChain().equals(line.split(" ")[1])) {
                            ki.debug("Found this one: " + height);
                            found++;
                        }
                        height = height.add(BigInteger.ONE);
                    }

                    ki.debug("Found " + found + " blocks");
                } else if (line.startsWith("findTransaction")) {
                    if (line.split(" ").length < 2) {
                        ki.debug("Not enough arguments");
                        continue;
                    }
                    ki.debug("Starting to look for transaction");
                    BigInteger height = BigInteger.ZERO;
                    String id = line.split(" ")[1];
                    while (height.compareTo(ki.getChainMan().currentHeight()) <= 0) {
                        for (String trans : ki.getChainMan().getByHeight(height).getTransactionKeys()) {
                            if (ki.getChainMan().getByHeight(height).getTransaction(trans).getID().equals(id)) {
                                ki.debug("Found instance in: " + height);
                                ki.debug("INFO:");
                                ki.debug("Inputs: ");
                                for (Input i : ki.getChainMan().getByHeight(height).getTransaction(trans).getInputs()) {
                                    ki.debug("ID: " + i.getID() + " Amount: " + i.getAmount());
                                }
                                ki.debug("Outputs: ");
                                for (Output o : ki.getChainMan().getByHeight(height).getTransaction(trans).getOutputs()) {
                                    ki.debug("ID: " + o.getID() + " Amount: " + o.getAmount());
                                }
                            }
                        }
                        height = height.add(BigInteger.ONE);
                    }
                    ki.debug("Done finding transaction instances");
                } else if (line.startsWith("findInput")) {
                    if (line.split(" ").length < 2) {
                        ki.debug("Not enough arguments");
                        continue;
                    }
                    ki.debug("Starting to look for input");
                    BigInteger height = BigInteger.ZERO;
                    String id = line.split(" ")[1];
                    while (height.compareTo(ki.getChainMan().currentHeight()) <= 0) {
                        for (String trans : ki.getChainMan().getByHeight(height).getTransactionKeys()) {


                            for (Input i : ki.getChainMan().getByHeight(height).getTransaction(trans).getInputs()) {
                                if (i.getID().equals(id)) {
                                    ki.debug("Found instance at: " + height);
                                }
                            }

                            for (Output o : ki.getChainMan().getByHeight(height).getTransaction(trans).getOutputs()) {
                                //ki.debug("Going over output: " + o.getID());
                                if (o.getID().equals(id)) {
                                    ki.debug("Found instance as output at: " + height);
                                }
                            }


                        }
                        if (ki.getChainMan().getByHeight(height).getCoinbase().getOutputs().get(0).getID().equals(id)) {
                            ki.debug("Found instance as coinbase: " + height);
                        }
                        height = height.add(BigInteger.ONE);
                    }
                } else if (line.startsWith("countOutputs")) {

                    BigInteger height = BigInteger.ZERO;
                    int amt = 0;
                    while (height.compareTo(ki.getChainMan().currentHeight()) <= 0) {
                        for (String trans : ki.getChainMan().getByHeight(height).getTransactionKeys()) {


                            for (Output o : ki.getChainMan().getByHeight(height).getTransaction(trans).getOutputs()) {

                                amt++;
                            }


                        }
                        height = height.add(BigInteger.ONE);
                    }
                    ki.debug("Amount: " + amt);
                } else if (line.contains("getThreads")) {
                    int t = 0;
                    Map<String, Integer> threads = new HashMap<>();
                    for (Thread thread : Thread.getAllStackTraces().keySet()) {
                        if (thread.isAlive()) {
                            t++;
                            if (threads.get(thread.getName()) != null) {
                                threads.put(thread.getName(), threads.get(thread.getName()) + 1);
                            } else {
                                threads.put(thread.getName(), 1);
                            }
                        }
                    }
                    ki.debug("Number of active threads: " + t);
                    ki.debug("Names and amounts:");
                    for (Map.Entry<String, Integer> name : threads.entrySet()) {
                        ki.debug(name.getKey() + ":" + name.getValue());
                    }
                    System.out.println("isDaemon status:");
                    for (Thread thread : Thread.getAllStackTraces().keySet()) {
                        System.out.println(thread.getName() + " : " + thread.isDaemon());
                    }
                    if (ManagementFactory.getThreadMXBean().isThreadCpuTimeSupported()) {
                        ki.debug("CPU Times");
                        for (long id : ManagementFactory.getThreadMXBean().getAllThreadIds()) {
                            long time = ManagementFactory.getThreadMXBean().getThreadCpuTime(id);
                            ki.debug(ManagementFactory.getThreadMXBean().getThreadInfo(id).getThreadName() + ":" + format.format(time / 1000) + "s");
                        }
                    }
                    if (ManagementFactory.getThreadMXBean().isThreadContentionMonitoringEnabled()) {
                        ki.debug("Contention monitoring:");
                        for (long id : ManagementFactory.getThreadMXBean().getAllThreadIds()) {
                            ThreadInfo ti = ManagementFactory.getThreadMXBean().getThreadInfo(id);
                            ki.debug(ti.getThreadName() + ":" + ti.getBlockedTime());
                            ki.debug("\tLocked by: " + ti.getLockName());
                        }
                    }
                    ki.debug("Deadlocked Threads:");
                    if (ManagementFactory.getThreadMXBean().findDeadlockedThreads() != null)
                        for (long id : ManagementFactory.getThreadMXBean().findDeadlockedThreads()) {
                            ki.debug(ManagementFactory.getThreadMXBean().getThreadInfo(id).getThreadName() + " is deadlocked");
                        }

                } else if (line.equalsIgnoreCase("checkBalance")) {
                    ki.getMainLog().info("Balance in main wallet " + ki.getAddMan().getMainAdd().encodeForChain() + " is: \n" + (ki.getTransMan().getAmountInWallet(ki.getAddMan().getMainAdd(), Token.ORIGIN).doubleValue() / 100_000_000d) + " " + Token.ORIGIN.getName());
                } else if (line.contains("setDynamicPPS")) {
                    if (ki.getOptions().poolRelay) {
                        String[] args = line.split(" ");
                        if (args.length < 2) {
                            ki.getMainLog().info("Not enough arguments");
                            continue;
                        }
                        double rate = 0;
                        try {
                            rate = Double.parseDouble(args[1]);
                        } catch (Exception e) {
                            ki.getMainLog().info("Invalid rate " + args[1]);
                            continue;
                        }
                        ki.getMainLog().info("Setting dynamic fee rate to: " + rate + "%");
                        BigDecimal sd = new BigDecimal(GPUMiner.shareDiff);
                        BigDecimal cd = new BigDecimal(ki.getChainMan().getCurrentDifficulty());
                        long pps = (long) (((cd.divide(sd, 9, RoundingMode.HALF_DOWN).doubleValue() * ChainManager.blockRewardForHeight(ki.getChainMan().currentHeight()).longValueExact()) * (1 - (rate / 100))));
                        ki.getPoolManager().updateCurrentPayPerShare(pps);
                        ki.setStringSetting(StringSettings.POOL_FEE, "" + rate);
                        ki.setSetting(Settings.DYNAMIC_FEE, true);

                    } else {
                        ki.getMainLog().info("Not a pool relay");
                    }
                } else if (line.contains("setStaticPPS")) {
                    if (ki.getOptions().poolRelay) {
                        String[] args = line.split(" ");
                        if (args.length < 2) {
                            ki.getMainLog().info("Not enough arguments");
                            continue;
                        }
                        double rate = 0;
                        try {
                            rate = Double.parseDouble(args[1]);
                        } catch (Exception e) {
                            ki.getMainLog().info("Invalid rate");
                            continue;
                        }
                        ki.getMainLog().info("Setting static fee rate to: " + rate + " ORA");
                        long pps = (long) (rate * 100_000_000L);
                        ki.getPoolManager().updateCurrentPayPerShare(pps);
                        ki.setSetting(Settings.DYNAMIC_FEE, false);

                    } else {
                        ki.getMainLog().info("Not a pool relay");
                    }
                } else if (line.contains("checkMiners")) {
                    if (ki.getOptions().poolRelay) {
                        ki.getMainLog().info("There are " + ki.getPoolNet().getConnections().size() + " miners connected currently");
                    } else {
                        ki.getMainLog().info("Not a pool relay");
                    }
                } else if (line.contains("checkHashrate")) {
                    if (ki.getOptions().poolRelay) {
                        long totalHR = 0;
                        for (String ID : ki.getPoolData().hrMap.keySet()) {
                            totalHR += (ki.getPoolData().hrMap.get(ID) / 1000000);
                        }
                        ki.getMainLog().info("Current pool hashrate is: " + totalHR + " MH/s");
                    } else {
                        ki.getMainLog().info("Not a pool relay");
                    }
                } else if (line.contains("checkShares")) {
                    if (ki.getOptions().poolRelay) {
                        ki.getMainLog().info("There are " + ki.getPoolManager().getTotalSharesOfCurrentPayPeriod() + " shares this pay period");
                    } else {
                        ki.getMainLog().info("Not a pool relay");
                    }
                } else if (line.contains("checkPayout")) {
                    if (ki.getOptions().poolRelay) {
                        ki.getMainLog().info("Estimated next payout at current pps is: " + ki.getPoolManager().getTotalSharesOfCurrentPayPeriod() * (double) ki.getPoolManager().getCurrentPayPerShare() / 100_000_000D);
                    } else {
                        ki.getMainLog().info("Not a pool relay");
                    }
                } else if (line.contains("enablePPLNS")) {
                    if (ki.getOptions().poolRelay) {
                        ki.getMainLog().info("enabling PPLNS");
                        ki.setSetting(Settings.PPLNS_SERVER, true);
                    } else {
                        ki.getMainLog().info("Not a pool relay");
                    }
                } else if (line.contains("disablePPLNS")) {
                    if (ki.getOptions().poolRelay) {
                        ki.getMainLog().info("Disabling PPLNS");
                        ki.setSetting(Settings.PPLNS_SERVER, false);
                    } else {
                        ki.getMainLog().info("Not a pool relay");
                    }
                } else if (line.contains("setPayoutTime")) {
                    if (ki.getOptions().poolRelay) {
                        String[] args = line.split(" ");
                        if (args.length < 2) {
                            ki.getMainLog().info("Not enough arguments");
                            continue;
                        }
                        long payoutTime = 0;
                        try {
                            payoutTime = Long.parseLong(args[1]);
                        } catch (Exception e) {
                            ki.getMainLog().info("Invalid payout time");
                            continue;
                        }
                        if (payoutTime < 1 || payoutTime > 720) {
                            ki.getMainLog().info("Invalid payout time, range must be between 1 and 720 minutes");
                            continue;
                        }
                        ki.getMainLog().info("Setting payout time to " + args[1] + " minutes");
                        ki.getPoolManager().updateCurrentPayInterval(payoutTime * 60_000L);
                    } else {
                        ki.getMainLog().info("Not a pool relay");
                    }
                } else if (line.contains("checkPPS")) {
                    if (ki.getOptions().poolRelay) {
                        if (ki.getSetting(Settings.DYNAMIC_FEE)) {
                            ki.getMainLog().info("Current dynamic fee rate: " + ki.getStringSetting(StringSettings.POOL_FEE));
                        } else {
                            ki.getMainLog().info("Current PPS: " + ki.getPoolManager().getCurrentPayPerShare());
                        }
                    } else {
                        ki.getMainLog().info("Not a pool relay");
                    }
                } else if (line.equalsIgnoreCase("pauseDebug")) {
                    ki.setSetting(Settings.DEBUG_MODE, false);
                } else if (line.equalsIgnoreCase("unpauseDebug")) {
                    ki.setSetting(Settings.DEBUG_MODE, true);
                } else {
                    System.out.println("unrecognized input");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Input processed");
        }
    }
}
