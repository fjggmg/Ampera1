package com.lifeform.main.transactions;

import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.ChainManager;
import com.lifeform.main.data.JSONManager;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Bryan on 8/11/2017.
 */
public class TransactionManager implements ITransMan{

    private DB utxoDB;
    private DB utxoValueDB;
    private ConcurrentMap<String,String> utxoMap;
    private ConcurrentMap<String,Boolean> utxoSpent;
    private ConcurrentMap<String,String> utxoValueMap;
    private IKi ki;
    private List<ITrans> pending = new ArrayList<>();
    private List<Input> usedUTXOs = new ArrayList<>();
    public TransactionManager(IKi ki)
    {
        this.ki = ki;
        utxoDB = DBMaker.fileDB("utxo.dat").fileMmapEnableIfSupported().transactionEnable().make();
        utxoMap = utxoDB.hashMap("utxoDB", Serializer.STRING,Serializer.STRING).createOrOpen();
        utxoSpent = utxoDB.hashMap("utxoDBSpent", Serializer.STRING,Serializer.BOOLEAN).createOrOpen();
        utxoValueDB = DBMaker.fileDB("utxoValue.dat").fileMmapEnableIfSupported().transactionEnable().make();
        utxoValueMap = utxoValueDB.hashMap("utxoValueDB",Serializer.STRING,Serializer.STRING).createOrOpen();
    }

    @Override
    public boolean verifyTransaction(ITrans transaction) {
        ki.debug("Verifying transaction: " + transaction.getID());
        for(Input i:transaction.getInputs())
        {
            ki.debug("Verifying input");
            if(utxoSpent.get(i.getID())) return false;
            ki.debug("input not spent");
            if(new BigInteger(utxoValueMap.get(i.getID())).compareTo(i.getAmount()) != 0) return false;
            ki.debug("input correct amount");
        }
        ki.debug("all inputs verified");
        if(!transaction.verifyInputToOutput()) return false;
        ki.debug("input to output verifies");
        if(!transaction.verifyCanSpend()) return false;
        ki.debug("verified can spend");
        if(!transaction.verifySigs()) return false;
        ki.debug("verified signature");
        return true;
    }

    @Override
    public boolean addTransaction(ITrans transaction) {
        if(!verifyTransaction(transaction)) return false;

        for(Input i:transaction.getInputs())
        {
            utxoSpent.put(i.getID(), true);
            if (utxoMap.get(i.getAddress().encodeForChain()) != null) {
                List<String> inputs = JSONManager.parseJSONToList(utxoMap.get(i.getAddress().encodeForChain()));
                inputs.remove(i.toJSON());
                utxoMap.put(i.getAddress().encodeForChain(), JSONManager.parseListToJSON(inputs).toJSONString());

            } else {
                List<String> inputs = new ArrayList<>();
                inputs.remove(i.toJSON());
                utxoMap.put(i.getAddress().encodeForChain(), JSONManager.parseListToJSON(inputs).toJSONString());
            }
        }
        for(Output o:transaction.getOutputs())
        {
            ki.getAddMan().receivedOn(o.getAddress());
            utxoSpent.put(o.getID(),false);
            utxoValueMap.put(o.getID(),o.getAmount().toString());
            if (utxoMap.get(o.getAddress().encodeForChain()) != null) {
                List<String> inputs = JSONManager.parseJSONToList(utxoMap.get(o.getAddress().encodeForChain()));
                inputs.add(o.toJSON());
                utxoMap.put(o.getAddress().encodeForChain(), JSONManager.parseListToJSON(inputs).toJSONString());

            } else {
                List<String> inputs = new ArrayList<>();
                inputs.add(o.toJSON());
                utxoMap.put(o.getAddress().encodeForChain(), JSONManager.parseListToJSON(inputs).toJSONString());
            }
        }
        utxoValueDB.commit();
        utxoDB.commit();
        List<ITrans> toRemove = new ArrayList<>();
        for(ITrans t: pending)
        {
            if(t.getID().equals(transaction.getID())) toRemove.add(t);
        }
        pending.removeAll(toRemove);
        return true;
    }

    @Override
    public List<Output> getUTXOs(Address address) {
        if (utxoMap.get(address.encodeForChain()) != null) {
            List<Output> utxos = new ArrayList<>();
            List<String> sUtxos = JSONManager.parseJSONToList(utxoMap.get(address.encodeForChain()));
            //ki.debug("List of UTXOs " + sUtxos);
            List<String> toRemove = new ArrayList<>();

            if(sUtxos != null) {
                Set<String> hs = new HashSet<>();
                hs.addAll(sUtxos);
                sUtxos.clear();
                sUtxos.addAll(hs);

                for (String s : sUtxos) {

                    if (!utxoSpent.get(Output.fromJSON(s).getID())) {
                        if(!usedUTXOs.contains(Input.fromOutput(Output.fromJSON(s))))
                        utxos.add(Output.fromJSON(s));
                    }
                    else
                        toRemove.add(s);
                }
                if (!toRemove.isEmpty()) {
                    sUtxos.removeAll(toRemove);
                    utxoMap.put(address.encodeForChain(), JSONManager.parseListToJSON(sUtxos).toJSONString());
                    utxoDB.commit();
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
        if(blockHeight.compareTo(BigInteger.ZERO) != 0)
        {
            if(transaction.getOutputs().size() > 1) return false;

            if(!transaction.getOutputs().get(0).getToken().equals(Token.ORIGIN)) return false;

            if(transaction.getOutputs().get(0).getAmount().compareTo(ChainManager.blockRewardForHeight(blockHeight).add(fees)) != 0) return false;



        }
        return true;
    }

    @Override
    public boolean addCoinbase(ITrans transaction, BigInteger blockHeight, BigInteger fees) {

        if(!verifyCoinbase(transaction,blockHeight,fees)) return false;

        for(Output o:transaction.getOutputs())
        {
            ki.debug("Address " + o.getAddress().encodeForChain());
            ki.debug("ID: " + o.getID());
            ki.debug("Token " + o.getToken());
            ki.debug("Amount " + o.getAmount());
            utxoSpent.put(o.getID(),false);
            utxoValueMap.put(o.getID(),o.getAmount().toString());
            ki.getAddMan().receivedOn(o.getAddress());
            if (utxoMap.get(o.getAddress().encodeForChain()) != null) {
                List<String> inputs = JSONManager.parseJSONToList(utxoMap.get(o.getAddress().encodeForChain()));
                inputs.add(o.toJSON());
                utxoMap.put(o.getAddress().encodeForChain(), JSONManager.parseListToJSON(inputs).toJSONString());

            } else {
                List<String> inputs = new ArrayList<>();
                inputs.add(o.toJSON());
                utxoMap.put(o.getAddress().encodeForChain(), JSONManager.parseListToJSON(inputs).toJSONString());
            }
        }
        utxoValueDB.commit();
        utxoDB.commit();

        return true;
    }

    @Override
    public List<ITrans> getPending() {
        return pending;
    }

    @Override
    public List<Input> getUsedUTXOs() {
        return usedUTXOs;
    }

    @Override
    public void close() {
        utxoDB.close();
        utxoValueDB.close();
    }
}
