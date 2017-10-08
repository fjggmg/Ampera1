package com.lifeform.main.transactions;

import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.ChainManager;
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
        ki.debug("Verifying transaction: " + transaction.getID());

        for (Input i : transaction.getInputs()) {
            ki.debug("Verifying input");
            if (utxoSpent == null) {
                ki.debug("UTXO file uninitialized, installation corrupted or fatal program error");
                return false;
            }
            if (i == null) {
                ki.debug("Input is null, malformed transaction.");
                return false;
            }
            if (utxoSpent.get(i.getID())) {
                ki.debug("Input already spent, bad transaction");
                return false;
            }
            ki.debug("input not spent");

            if (new BigInteger(utxoValueMap.get(i.getID())).compareTo(i.getAmount()) != 0) {
                ki.debug("input is incorrect amount");
                return false;
            }
            ki.debug("input correct amount");
        }
        ki.debug("all inputs verified");
        if (!transaction.verifyInputToOutput()) {
            ki.debug("Input values are not equal to output values");
            return false;
        }
        ki.debug("input to output verifies");
        if (!transaction.verifyCanSpend()) {
            ki.debug("this address cannot spend this input");
            return false;
        }
        ki.debug("verified can spend");
        if (!transaction.verifySigs()) {
            ki.debug("the signature on this transaction does not match");
            return false;
        }
        ki.debug("verified signature");
        ki.debug("Transaction verified");
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
        ki.debug("Saving transaction to disk");
        ki.debug("Transaction has: " + transaction.getInputs().size() + " inputs");
        List<String> inputs;
        for (Input i : transaction.getInputs()) {

            ki.debug("Saving input: " + i.getID());
            utxoSpent.put(i.getID(), true);
            Object carry = utxoMap.get(i.getAddress().encodeForChain());
            if (carry != null) {
                inputs = JSONManager.parseJSONToList((String)carry);

                inputs.remove(i.toJSON());
                utxoMap.put(i.getAddress().encodeForChain(), JSONManager.parseListToJSON(inputs).toJSONString());

            } else {
                //TODO this should never activate.....investigate logic in other areas to find why we still have this here. It is used in the output saving, but should not in the input saving
                inputs = new ArrayList<>();
                utxoMap.put(i.getAddress().encodeForChain(), JSONManager.parseListToJSON(inputs).toJSONString());
            }
        }
        ki.debug("Transaction has: " + transaction.getOutputs().size() + " outputs");
        for (Output o : transaction.getOutputs()) {
            ki.debug("Saving output: " + o.getID() + " Token: " + o.getToken() + " Amount: " + o.getAmount());
            ki.getAddMan().receivedOn(o.getAddress());
            utxoSpent.put(o.getID(), false);
            utxoValueMap.put(o.getID(), o.getAmount().toString());
            Object carry = utxoMap.get(o.getAddress().encodeForChain());
            if (carry != null) {
                inputs = JSONManager.parseJSONToList((String) carry);
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
        ki.debug("Transaction removed from pending pool, done adding transaction");
        return true;
    }

    @Override
    public List<Output> getUTXOs(Address address) {
        if (utxoMap.get(address.encodeForChain()) != null) {
            List<Output> utxos = new ArrayList<>();
            List<String> sUtxos = JSONManager.parseJSONToList(utxoMap.get(address.encodeForChain()));
            //ki.debug("List of UTXOs " + sUtxos);
            List<String> toRemove = new ArrayList<>();

            if (sUtxos != null) {
                Set<String> hs = new HashSet<>();
                hs.addAll(sUtxos);
                sUtxos.clear();
                sUtxos.addAll(hs);

                for (String s : sUtxos) {
                    if (!utxoSpent.get(Output.fromJSON(s).getID())) {
                        if (!usedUTXOs.contains(Input.fromOutput(Output.fromJSON(s)).getID()))
                            utxos.add(Output.fromJSON(s));
                    } else
                        toRemove.add(s);
                }
                if (!toRemove.isEmpty()) {
                    sUtxos.removeAll(toRemove);
                    utxoMap.put(address.encodeForChain(), JSONManager.parseListToJSON(sUtxos).toJSONString());

                }
            }
            return utxos;
        }
        return null;
    }

    @Override
    public boolean verifyCoinbase(ITrans transaction, BigInteger blockHeight, BigInteger fees) {
        ki.debug("Verifying coinbase transaction");
        ki.debug("It has: " + transaction.getOutputs().size() + " outputs");
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
            ki.debug("Address " + o.getAddress().encodeForChain());
            ki.debug("ID: " + o.getID());
            ki.debug("Token " + o.getToken());
            ki.debug("Amount " + o.getAmount());
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
