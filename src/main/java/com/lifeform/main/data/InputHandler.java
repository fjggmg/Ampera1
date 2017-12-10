package com.lifeform.main.data;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.network.Ping;
import com.lifeform.main.network.TransactionDataRequest;
import com.lifeform.main.network.TransactionPacket;
import com.lifeform.main.transactions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class InputHandler extends Thread {

    private IKi ki;

    public InputHandler(IKi ki) {
        this.ki = ki;
    }

    @Override
    public void run() {
        setName("CommandLine Input");
        BufferedReader s = new BufferedReader(new InputStreamReader(System.in));
        while (true) {


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
                        Address receiver = Address.decodeFromChain(lines[1]);
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
                            Output output = new Output(amount, receiver, token, index, System.currentTimeMillis());
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
                            for (Address a : ki.getAddMan().getActive()) {
                                if (ki.getTransMan().getUTXOs(a) == null) return;
                                for (Output o : ki.getTransMan().getUTXOs(a)) {
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
                            for (Address a : ki.getAddMan().getActive()) {
                                //get inputs
                                if (feeInput.compareTo(fee) >= 0) break;
                                for (Output o : ki.getTransMan().getUTXOs(a)) {
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


                            ITrans trans = new Transaction(message, 1, null, outputs, inputs, entropyMap, keys);
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
                                tp.trans = trans.toJSON();
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
                    for (Address a : ki.getAddMan().getAll()) {
                        ki.getMainLog().info(a.encodeForChain());
                    }
                    tdr.addresses = ki.getAddMan().getAll();
                    ki.getNetMan().broadcast(tdr);


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
