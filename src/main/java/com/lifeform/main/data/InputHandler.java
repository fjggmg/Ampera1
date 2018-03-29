package com.lifeform.main.data;

import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.blockchain.BlockState;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.network.Ping;
import com.lifeform.main.network.TransactionDataRequest;
import com.lifeform.main.network.TransactionPacket;
import com.lifeform.main.transactions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class InputHandler extends Thread {

    private IKi ki;

    public InputHandler(IKi ki) {
        this.ki = ki;
    }

    DecimalFormat format = new DecimalFormat("###,###,###,###,###,###,###");
    @Override
    public void run() {
        setName("CommandLine Input");
        BufferedReader s = new BufferedReader(new InputStreamReader(System.in));
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
                    System.exit(0);
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
                    String[] lines = line.split(" ");
                    if (lines.length < 4) {
                        ki.getMainLog().info("Not enough arguments");
                    } else {
                        IAddress receiver = Address.decodeFromChain(lines[1]);
                        if (receiver.isValid()) {
                            double amt;
                            try {
                                amt = Double.parseDouble(lines[2]);

                            } catch (Exception e) {
                                ki.getMainLog().info("Invalid amount");
                                continue;
                            }
                            Token token;
                            try {
                                token = Token.valueOf(lines[3]);
                            } catch (Exception e) {
                                ki.getMainLog().info("Invalid token");
                                continue;
                            }

                            double dFee = 0;
                            if (lines.length >= 5) {
                                try {
                                    dFee = Double.parseDouble(lines[4]);
                                } catch (Exception e) {
                                    ki.getMainLog().info("Invalid fee");
                                    continue;
                                }
                            }
                            StringBuilder messageBuilder = new StringBuilder();
                            if (lines.length >= 6) {
                                for (int i = 5; i < lines.length; i++) {
                                    messageBuilder.append(lines[i]);
                                    messageBuilder.append(" ");
                                }
                            }
                            String message = messageBuilder.toString();

                            long lAmt = (long) (amt * 100000000D);
                            BigInteger amount = BigInteger.valueOf(lAmt);
                            int index = 0;
                            Output output = new Output(amount, receiver, token, index, System.currentTimeMillis(), (byte) 2);
                            java.util.List<Output> outputs = new ArrayList<>();
                            outputs.add(output);
                            java.util.List<String> keys = new ArrayList<>();
                            keys.add(ki.getEncryptMan().getPublicKeyString());
                            java.util.List<Input> inputs = new ArrayList<>();
                            BigInteger fee;

                            long lFee = (long) (dFee * 100000000D);
                            fee = BigInteger.valueOf(lFee);

                            ki.getMainLog().info("Fee is: " + fee.toString());

                            BigInteger totalInput = BigInteger.ZERO;
                            for (IAddress a : ki.getAddMan().getActive()) {
                                if (ki.getTransMan().getUTXOs(a, true) == null) return;
                                for (Output o : ki.getTransMan().getUTXOs(a, true)) {
                                    if (o.getToken().equals(token)) {
                                        if (inputs.contains(Input.fromOutput(o))) continue;
                                        inputs.add(Input.fromOutput(o));
                                        totalInput = totalInput.add(o.getAmount());
                                        if (totalInput.compareTo(amount) >= 0) break;

                                    }
                                }
                                if (totalInput.compareTo(amount) >= 0) break;

                            }
                            if (totalInput.compareTo(amount) < 0) {
                                ki.getMainLog().info("Not enough " + token.name() + " to do this transaction");
                                return; // not enough of this token to send;
                            }

                            BigInteger feeInput = (token.equals(Token.ORIGIN)) ? totalInput : BigInteger.ZERO;
                            for (IAddress a : ki.getAddMan().getActive()) {
                                //get inputs
                                if (feeInput.compareTo(fee) >= 0) break;
                                for (Output o : ki.getTransMan().getUTXOs(a, true)) {
                                    if (o.getToken().equals(Token.ORIGIN)) {
                                        inputs.add(Input.fromOutput(o));
                                        feeInput = feeInput.add(o.getAmount());
                                        if (feeInput.compareTo(fee) >= 0) break;

                                    }
                                }


                            }

                            if (feeInput.compareTo(fee) < 0) {
                                ki.getMainLog().info("Not enough origin to pay for this fee");
                                continue; //not enough origin to send this kind of fee
                            }

                            Map<String, String> entropyMap = new HashMap<>();

                            for (Input i : inputs) {
                                if (entropyMap.containsKey(i.getAddress().encodeForChain())) continue;
                                entropyMap.put(i.getAddress().encodeForChain(), ki.getAddMan().getEntropyForAdd(i.getAddress()));
                                ki.getMainLog().info("Matching: " + i.getAddress().encodeForChain() + " with " + ki.getAddMan().getEntropyForAdd(i.getAddress()));
                            }


                            ITrans trans = new Transaction(message, 1, null, outputs, inputs, entropyMap, keys, TransactionType.STANDARD);
                            ki.debug("Transaction has: " + trans.getOutputs().size() + " Outputs before finalization");
                            trans.makeChange(fee, ki.getAddMan().getMainAdd()); // TODO this just sends change back to the main address......will need to give option later
                            trans.addSig(ki.getEncryptMan().getPublicKeyString(), ki.getEncryptMan().sign(trans.toSign()));
                            ki.debug("Transaction has: " + trans.getOutputs().size() + "Outputs after finalization");
                            if (ki.getTransMan().verifyTransaction(trans)) {
                                ki.getTransMan().getPending().add(trans);
                                for (Input i : trans.getInputs()) {
                                    ki.getTransMan().getUsedUTXOs().add(i.getID());
                                }
                                TransactionPacket tp = new TransactionPacket();
                                tp.trans = trans.serializeToAmplet().serializeToBytes();
                                ki.getNetMan().broadcast(tp);
                            } else {
                                ki.debug("Transaction did not verify, not sending and not adding to pending list");
                            }

                        } else {
                            ki.getMainLog().info("Invalid address");
                        }

                    }
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
                    if (ki.getTransMan().getUTXOs(Address.decodeFromChain(line.split(" ")[1]), true) != null)
                        for (Output out : ki.getTransMan().getUTXOs(Address.decodeFromChain(line.split(" ")[1]), true)) {
                            ki.debug("Output data : " + out.getID() + " Address: " + out.getAddress().encodeForChain() + " Amount: " + out.getAmount());
                        }
                    ki.debug("Done getting UTXOs");


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


                    Map<BigInteger, Block> chain = new HashMap<>();

                    BigInteger h = BigInteger.ZERO;
                    while (h.compareTo(ki.getChainMan().currentHeight()) <= 0) {

                        chain.put(new BigInteger(h.toByteArray()), ki.getChainMan().getByHeight(h));
                        h = h.add(BigInteger.ONE);
                    }
                    BigInteger height = BigInteger.ZERO;
                    ki.getChainMan().setDiff(new BigInteger("00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16));
                    boolean success = true;
                    BigInteger height2 = new BigInteger(ki.getChainMan().currentHeight().toByteArray());
                    ki.getChainMan().undoToBlock(height);
                    ki.getTransMan().clear();


                    while (height.compareTo(height2) <= 0) {
                        ki.debug("Rebuilding block: " + height);
                        BlockState bs = ki.getChainMan().addBlock(chain.get(height));
                        if (!bs.success()) {
                            ki.debug("Failed: block state: " + bs);
                            success = false;
                            break;
                        }
                        height = height.add(BigInteger.ONE);
                    }
                    ki.debug("Done rebuilding result: " + ((success) ? "Success" : "Failure"));

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
                    for (String name : threads.keySet()) {
                        ki.debug(name + ":" + threads.get(name));
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

                } else {
                    System.out.println("unrecognized input");
                }
            } catch (IOException | InvalidTransactionException e) {
                e.printStackTrace();
            }

            System.out.println("Input processed");
        }
    }
}
