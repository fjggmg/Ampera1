package com.lifeform.main.transactions;

import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.ChainManager;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.JSONManager;
import com.lifeform.main.data.XodusStringBooleanMap;
import com.lifeform.main.data.XodusStringMap;
import java.io.File;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Bryan on 8/11/2017.
 */
public class TransactionManager implements ITransMan {


    private XodusStringMap utxoMap;
    private XodusStringBooleanMap utxoSpent;
    private XodusStringMap utxoValueMap;
    private IKi ki;
    private List<ITrans> pending = new CopyOnWriteArrayList<>();
    private List<String> usedUTXOs = new ArrayList<>();

    public TransactionManager(IKi ki, boolean dump) {
        this.ki = ki;
        new File("transactions" + ki.getChainMan().getChainVer() + "/").mkdirs();
        utxoMap = new XodusStringMap("transactions" + ki.getChainMan().getChainVer() + "/utxo.dat");//utxoDB.hashMap("utxoDB", Serializer.STRING, Serializer.STRING).createOrOpen();
        utxoSpent = new XodusStringBooleanMap("transactions" + ki.getChainMan().getChainVer() + "/utxoSpent.dat");//utxoDB.hashMap("utxoDBSpent", Serializer.STRING, Serializer.BOOLEAN).createOrOpen();
        utxoValueMap = new XodusStringMap("transactions" + ki.getChainMan().getChainVer() + "/utxoValue.dat");//utxoValueDB.hashMap("utxoValueDB", Serializer.STRING, Serializer.STRING).createOrOpen();

        new Thread() {
            public void run() {
                List<ITrans> toRemove = new ArrayList<>();
                setName("Transaction Cleanup");
                while (true) {
                    for (ITrans t : pending) {
                        if (t.getOutputs().get(0).getTimestamp() < System.currentTimeMillis() - 3_600_000) {
                            toRemove.add(t);
                        }
                    }
                    if (!toRemove.isEmpty()) {
                        pending.removeAll(toRemove);
                        toRemove.clear();
                    }

                    try {
                        sleep(3_600_0000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        if (dump) {
            /*
            StringFileHandler fh = new StringFileHandler(ki,"utxoDump.txt");
            fh.delete();
            for(String key:utxoMap.keySet())
            {

                fh.addLine("Address: " + key);
                List<String> dumpList = JSONManager.parseJSONToList(utxoMap.get(key));
                for(String value:dumpList)
                {
                    fh.addLine(value);
                }

            }
            fh.save();
            fh = new StringFileHandler(ki,"utxoValueDump.txt");
            fh.delete();
            for(String key:utxoValueMap.keySet())
            {
                fh.addLine(key + " " + utxoValueMap.get(key));
            }
            fh.save();

            fh = new StringFileHandler(ki,"utxoSpentDump.txt");
            fh.delete();
            for(String key:utxoSpent.keySet())
            {
                fh.addLine(key + " " + utxoSpent.get(key));
            }
            fh.save();
            */
            //old dump does not work
        }
    }

    @Override
    public boolean verifyTransaction(ITrans transaction) {
        if (ki.getOptions().tDebug)
            ki.debug("Verifying transaction: " + transaction.getID());

        for (Input i : transaction.getInputs()) {
            if (ki.getOptions().tDebug)
                ki.debug("Verifying input: " + i.getID());
            if (utxoSpent == null) {
                if (ki.getOptions().tDebug)
                    ki.debug("UTXO file uninitialized, installation corrupted or fatal program error");
                return false;
            }
            if (i == null) {
                if (ki.getOptions().tDebug)
                    ki.debug("Input is null, malformed transaction.");
                return false;
            } else {
                //ki.debug("Found in spend database");
            }
            if ((utxoSpent.get(i.getID()) == null)) {
                if (ki.getOptions().tDebug)
                    ki.debug("Input is null in spend db");
            }
            if (utxoSpent.get(i.getID()) == null || utxoSpent.get(i.getID())) {
                if (ki.getOptions().tDebug)
                    ki.debug("Input already spent, bad transaction");
                return false;
            }
            if (ki.getOptions().tDebug)
                ki.debug("input not spent");

            if (new BigInteger(utxoValueMap.get(i.getID())).compareTo(i.getAmount()) != 0) {
                if (ki.getOptions().tDebug)
                    ki.debug("input is incorrect amount");
                return false;
            }
            if (ki.getOptions().tDebug)
                ki.debug("input correct amount");
        }
        if (ki.getOptions().tDebug)
            ki.debug("all inputs verified");
        if (!transaction.verifyInputToOutput()) {
            if (ki.getOptions().tDebug)
                ki.debug("Input values are not equal to output values");
            return false;
        }
        if (ki.getOptions().tDebug)
            ki.debug("input to output verifies");
        if (!transaction.verifyCanSpend()) {
            if (ki.getOptions().tDebug)
                ki.debug("this address cannot spend this input");
            return false;
        }
        if (ki.getOptions().tDebug)
            ki.debug("verified can spend");
        if (!transaction.verifySigs()) {
            if (ki.getOptions().tDebug)
                ki.debug("the signature on this transaction does not match");
            return false;
        }
        if (!transaction.verifySpecial()) {
            if (ki.getOptions().tDebug)
                ki.debug("Special requirements for this transaction have not been met");
            return false;
        }
        if (ki.getOptions().tDebug) {
            ki.debug("verified signature");
            ki.debug("Transaction verified");
        }
        return true;
    }

    @Override
    public boolean addTransaction(ITrans transaction) {
        return verifyTransaction(transaction) && addTransactionNoVerify(transaction);

    }

    /**
     * very dangerous method, only use when you are certain the transaction you are adding is valid
     *
     * @param transaction
     * @return true if successful
     */
    @Override
    public boolean addTransactionNoVerify(ITrans transaction) {
        if (ki.getOptions().tDebug)
            ki.debug("Saving transaction to disk");
        if (ki.getOptions().tDebug)
            ki.debug("Transaction has: " + transaction.getInputs().size() + " inputs");
        List<String> inputs = new ArrayList<>();
        String carry = null;
        String lastAdd = "";
        boolean sameAdd = false;
        for (Input i : transaction.getInputs()) {
            if (ki.getOptions().tDebug)
            ki.debug("Saving input: " + i.getID());
            utxoSpent.put(i.getID(), true);
            if(lastAdd.equals(i.getAddress().encodeForChain()))
                sameAdd = true;
            if(!sameAdd)
            carry = utxoMap.get(i.getAddress().encodeForChain());
            if (carry != null) {
                if(!sameAdd)
                inputs = JSONManager.parseJSONToList(carry);

                inputs.remove(i.toJSON());
                utxoMap.put(i.getAddress().encodeForChain(), JSONManager.parseListToJSON(inputs).toJSONString());

            } else {
                //TODO this should never activate.....investigate logic in other areas to find why we still have this here. It is used in the output saving, but should not in the input saving
                inputs = new ArrayList<>();
                utxoMap.put(i.getAddress().encodeForChain(), JSONManager.parseListToJSON(inputs).toJSONString());
            }
            lastAdd = i.getAddress().encodeForChain();
        }
        lastAdd = "";
        sameAdd = false;
        carry = "";
        if (ki.getOptions().tDebug)
            ki.debug("Transaction has: " + transaction.getOutputs().size() + " outputs");
        for (Output o : transaction.getOutputs()) {
            if (ki.getOptions().tDebug)
                ki.debug("Saving output: " + o.getID() + " Token: " + o.getToken() + " Amount: " + o.getAmount());
            ki.getAddMan().receivedOn(o.getAddress());
            utxoSpent.put(o.getID(), false);
            utxoValueMap.put(o.getID(), o.getAmount().toString());
            if(lastAdd.equals(o.getAddress().encodeForChain()))
                sameAdd = true;
            if(!sameAdd)
            carry = utxoMap.get(o.getAddress().encodeForChain());
            if (carry != null) {
                if(!sameAdd)
                inputs = JSONManager.parseJSONToList(carry);
                inputs.add(o.toJSON());
                utxoMap.put(o.getAddress().encodeForChain(), JSONManager.parseListToJSON(inputs).toJSONString());
            } else {
                inputs = new ArrayList<>();
                inputs.add(o.toJSON());
                utxoMap.put(o.getAddress().encodeForChain(), JSONManager.parseListToJSON(inputs).toJSONString());
            }
        }

        List<ITrans> toRemove = new ArrayList<>();
        for (ITrans t : pending) {
            if (t.getID().equals(transaction.getID())) toRemove.add(t);
        }
        pending.removeAll(toRemove);
        if (ki.getOptions().tDebug)
            ki.debug("Transaction removed from pending pool, done adding transaction");
        return true;
    }

    List<Output> utxos = new ArrayList<>();
    List<String> sUtxos;
    List<String> toRemove = new ArrayList<>();
    Set<String> hs = new HashSet<>();
    private static volatile boolean lock = false;
    @Override
    public List<Output> getUTXOs(Address address, boolean safe) {
        while (lock) {
        }
        lock = true;
        if (utxoMap.get(address.encodeForChain()) == null) {
            lock = false;
            return null;
        }
        sUtxos = JSONManager.parseJSONToList(utxoMap.get(address.encodeForChain()));
        if (sUtxos != null && !sUtxos.isEmpty()) {
            if (!safe)
                utxos.clear();
            else
                utxos = new ArrayList<>();
            toRemove.clear();
            if (sUtxos != null && !sUtxos.isEmpty()) {
                hs.clear();
                hs.addAll(sUtxos);
                sUtxos.clear();
                sUtxos.addAll(hs);
                for (String s : sUtxos) {
                    Output o;
                    try {
                        o = Output.fromJSON(s);
                    } catch (Exception e) {
                        continue;
                    }
                    if (!utxoSpent.get(o.getID())) {
                        if (!usedUTXOs.contains(Input.fromOutput(o).getID()))
                            utxos.add(o);
                    } else
                        toRemove.add(s);
                }
                if (!toRemove.isEmpty()) {
                    sUtxos.removeAll(toRemove);
                    utxoMap.put(address.encodeForChain(), JSONManager.parseListToJSON(sUtxos).toJSONString());
                }
            }
            lock = false;
            return utxos;
        }
        lock = false;
        return null;
    }

    @Override
    public boolean verifyCoinbase(ITrans transaction, BigInteger blockHeight, BigInteger fees) {
        if (ki.getOptions().tDebug) {
            ki.debug("Verifying coinbase transaction");
            ki.debug("It has: " + transaction.getOutputs().size() + " outputs");
        }
        if (blockHeight.compareTo(BigInteger.ZERO) != 0) {
            if (transaction.getOutputs().size() > 1) return false;

            if (!transaction.getOutputs().get(0).getToken().equals(Token.ORIGIN)) return false;

            if (transaction.getOutputs().get(0).getAmount().compareTo(ChainManager.blockRewardForHeight(blockHeight).add(fees)) != 0)
                return false;


        }
        return true;
    }

    @Override
    public boolean addCoinbase(ITrans transaction, BigInteger blockHeight, BigInteger fees) {

        if (!verifyCoinbase(transaction, blockHeight, fees)) return false;
        for (Output o : transaction.getOutputs()) {
            if (ki.getOptions().tDebug) {
                ki.debug("Address " + o.getAddress().encodeForChain());
                ki.debug("ID: " + o.getID());
                ki.debug("Token " + o.getToken());
                ki.debug("Amount " + o.getAmount());
            }
            utxoSpent.put(o.getID(), false);
            utxoValueMap.put(o.getID(), o.getAmount().toString());
            ki.getAddMan().receivedOn(o.getAddress());
            if (utxoMap.get(o.getAddress().encodeForChain()) != null) {
                List<String> inputs = JSONManager.parseJSONToList(utxoMap.get(o.getAddress().encodeForChain()));
                if (inputs != null)
                    inputs.add(o.toJSON());
                else {
                    ki.debug("Problem adding coinbase transaction, UTXO map has a value for the solver key but it is null.");
                }
                utxoMap.put(o.getAddress().encodeForChain(), JSONManager.parseListToJSON(inputs).toJSONString());

            } else {
                List<String> inputs = new ArrayList<>();
                inputs.add(o.toJSON());
                utxoMap.put(o.getAddress().encodeForChain(), JSONManager.parseListToJSON(inputs).toJSONString());
            }
        }


        return true;
    }

    @Override
    public List<ITrans> getPending() {
        return pending;
    }

    @Override
    public List<String> getUsedUTXOs() {
        return usedUTXOs;
    }

    @Override
    public void undoTransaction(ITrans transaction) {
        if (ki.getOptions().tDebug)
            ki.debug("Saving transaction to disk");
        if (ki.getOptions().tDebug)
            ki.debug("Transaction has: " + transaction.getInputs().size() + " inputs");
        List<String> inputs = new ArrayList<>();
        String carry = null;
        String lastAdd = "";
        boolean sameAdd = false;
        for (Input i : transaction.getInputs()) {
            if (ki.getOptions().tDebug)
                ki.debug("Saving input: " + i.getID());
            utxoSpent.put(i.getID(), false);
            if (lastAdd.equals(i.getAddress().encodeForChain()))
                sameAdd = true;
            if (!sameAdd)
                carry = utxoMap.get(i.getAddress().encodeForChain());
            if (carry != null) {
                if (!sameAdd)
                    inputs = JSONManager.parseJSONToList(carry);

                inputs.add(i.toJSON());
                utxoMap.put(i.getAddress().encodeForChain(), JSONManager.parseListToJSON(inputs).toJSONString());

            } else {
                //TODO this should never activate.....investigate logic in other areas to find why we still have this here. It is used in the output saving, but should not in the input saving
                inputs = new ArrayList<>();
                utxoMap.put(i.getAddress().encodeForChain(), JSONManager.parseListToJSON(inputs).toJSONString());
            }
            lastAdd = i.getAddress().encodeForChain();
        }
        lastAdd = "";
        sameAdd = false;
        carry = "";
        if (ki.getOptions().tDebug)
            ki.debug("Transaction has: " + transaction.getOutputs().size() + " outputs");
        for (Output o : transaction.getOutputs()) {
            if (ki.getOptions().tDebug)
                ki.debug("Saving output: " + o.getID() + " Token: " + o.getToken() + " Amount: " + o.getAmount());
            ki.getAddMan().receivedOn(o.getAddress());
            utxoSpent.remove(o.getID());
            utxoValueMap.remove(o.getID());
            if (lastAdd.equals(o.getAddress().encodeForChain()))
                sameAdd = true;
            if (!sameAdd)
                carry = utxoMap.get(o.getAddress().encodeForChain());
            if (carry != null) {
                if (!sameAdd)
                    inputs = JSONManager.parseJSONToList(carry);
                inputs.remove(o.toJSON());
                utxoMap.put(o.getAddress().encodeForChain(), JSONManager.parseListToJSON(inputs).toJSONString());
            } else {
                inputs = new ArrayList<>();
                inputs.add(o.toJSON());
                utxoMap.put(o.getAddress().encodeForChain(), JSONManager.parseListToJSON(inputs).toJSONString());
            }
        }

        List<ITrans> toRemove = new ArrayList<>();
        for (ITrans t : pending) {
            if (t.getID().equals(transaction.getID())) toRemove.add(t);
        }
        pending.removeAll(toRemove);
        if (ki.getOptions().tDebug)
            ki.debug("Transaction removed from pending pool, done adding transaction");
    }

    private String lastHash = "";
    private String cHash;
    @Override
    public boolean utxosChanged(Address address) {
        cHash = "";
        if (utxoMap.get(address.encodeForChain()) != null)
            cHash = EncryptionManager.sha224(JSONManager.parseJSONToList(utxoMap.get(address.encodeForChain())).toString());
        if (cHash != null && cHash.equals(lastHash)) {
            return false;
        }
        lastHash = cHash;
        return true;
    }

    @Deprecated
    @Override
    public void commit() {

    }
    @Override
    public void close() {
        utxoMap.close();
        utxoValueMap.close();
        utxoSpent.close();
    }

    @Override
    public void clear() {
        utxoMap.clear();
        utxoValueMap.clear();
        utxoSpent.clear();

    }
}
