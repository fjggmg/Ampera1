package com.lifeform.main.transactions;

import com.lifeform.main.IKi;
import com.lifeform.main.data.JSONManager;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Bryan on 5/30/2017.
 */
public class TransactionManager  implements  ITransMan{

    private DB utxoDB;
    private DB utxoValueDB;
    private ConcurrentMap<String,String> utxoMap;
    private ConcurrentMap<String,Boolean> utxoSpent;
    private ConcurrentMap<String,String> utxoValueMap;
    private IKi ki;
    private Map<String,MKiTransaction> pending = new HashMap<>();
    public TransactionManager(IKi ki)
    {
        this.ki = ki;
        utxoDB = DBMaker.fileDB("utxo.dat").fileMmapEnableIfSupported().transactionEnable().make();
        utxoMap = utxoDB.hashMap("utxoDB", Serializer.STRING,Serializer.STRING).createOrOpen();
        utxoSpent = utxoDB.hashMap("utxoDBSpent", Serializer.STRING,Serializer.BOOLEAN).createOrOpen();
        utxoValueDB = DBMaker.fileDB("utxoValue.dat").fileMmapEnableIfSupported().transactionEnable().make();
        utxoValueMap = utxoValueDB.hashMap("utxoValueDB",Serializer.STRING,Serializer.STRING).createOrOpen();




    }

    public void close()
    {
        utxoDB.close();
        utxoValueDB.close();
    }

    @Override
    public ConcurrentMap<String, String> getUTXOMap() {
        return utxoMap;
    }

    @Override
    public DB getUTXODB() {
        return utxoDB;
    }

    @Override
    public ConcurrentMap<String, String> getUTXOValueMap() {
        return utxoValueMap;
    }



    @Override
    public DB getUTXOValueDB() {
        return utxoValueDB;
    }

    @Override
    public boolean verifyAndCommitTransaction(MKiTransaction s) {
        boolean success = verifyTransaction(s);
        if(success)
        {
            utxoDB.commit();
            return true;
        }else {
            return false;
        }
    }

    @Override
    public ConcurrentMap<String, Boolean> getUTXOSpentMap() {
        return utxoSpent;
    }

    @Override
    public boolean softVerifyTransaction(MKiTransaction t) {
        BigInteger inputs = BigInteger.ZERO;

        List<String> walletUTXO = JSONManager.parseJSONToList(utxoMap.get(t.sender));
        if(walletUTXO == null) return false; //no inputs
        ki.getMainLog().info("mark 1");
        for(String trans:walletUTXO)
        {
            ki.getMainLog().info("Trans in wallet: " + trans);
        }
        if(t.inputs.isEmpty())
        {
            ki.getMainLog().info("input empty");
            return false;
        }
        for(String trans: t.inputs.keySet())
        {
            ki.getMainLog().info("Input is: " + trans + "\n Amount: " + getUTXOValueMap().get(trans));
            if(!walletUTXO.contains(trans)) return false; //not your transaction
            ki.getMainLog().info("mark 2");
            //if(!utxoMap.containsKey(trans)) return false; //not unspent
            if(utxoSpent.get(trans)) return false; //not unspent
            ki.getMainLog().info("mark 3");
            inputs = inputs.add(new BigInteger(utxoValueMap.get(trans)));

            /*
            utxoMap.remove(trans);
            utxoValueMap.remove(trans);
            walletUTXO.remove(trans);
            utxoMap.put(t.sender,JSONManager.parseListToJSON(walletUTXO).toJSONString());
            */
        }

        if(!(inputs.compareTo(t.amount.add(t.relayFee).add(t.transactionFee)) >= 0)) return false; //not enough input
        ki.getMainLog().info("mark 4");
        if(inputs.subtract(t.amount.add(t.relayFee).add(t.transactionFee)).compareTo(t.change) != 0) return false; //CAN YOU NOT PERFORM SIMPLE MATH YOU FUCKING FAG-O-TRON
        ki.getMainLog().info("mark 5");
        if(!ki.getEncryptMan().verifySig(t.all(),t.signature,t.sender)) return false; //NOT YOUR TRANSACTION BROSKI
        ki.getMainLog().info("mark 6");
        if(!ki.getEncryptMan().verifySig(t.preSigAll(),t.relaySignature,t.relayer)) return false; //WHO THE FUCK RELAYED THIS
        ki.getMainLog().info("mark 7");

        return true;
    }

    public BigInteger entryHeight = BigInteger.ZERO;
    @Override
    public Map<String,MKiTransaction> getPending()
    {
        return pending;
    }

    @Override
    public Map<String, BigInteger> getInputs(String key) {
        Map<String,BigInteger> inputs = new HashMap<>();

        List<String> IDs = JSONManager.parseJSONToList(utxoMap.get(key));
        if(IDs == null) return null;
        for(String ID:IDs)
        {
            if(!getUTXOSpentMap().get(ID))
            inputs.put(ID,new BigInteger(getUTXOValueMap().get(ID)));
        }


        return inputs;
    }



    @Override
    public boolean verifyTransaction(MKiTransaction t) {

        BigInteger inputs = BigInteger.ZERO;

        List<String> walletUTXO = JSONManager.parseJSONToList(utxoMap.get(t.sender));
        if(walletUTXO == null) return false; //no inputs
        ki.getMainLog().info("mark 1");

        for(String trans: t.inputs.keySet())
        {
            if(!walletUTXO.contains(trans)) return false; //not your transaction
            ki.getMainLog().info("mark 2");
            //if(!utxoMap.containsKey(trans)) return false; //not unspent
            if(utxoSpent.get(trans)) return false; //not unspent
            ki.getMainLog().info("mark 3");
            inputs = inputs.add(new BigInteger(utxoValueMap.get(trans)));
            utxoSpent.put(trans,true);
            /*
            utxoMap.remove(trans);
            utxoValueMap.remove(trans);
            walletUTXO.remove(trans);
            utxoMap.put(t.sender,JSONManager.parseListToJSON(walletUTXO).toJSONString());
            */
        }



        if(!(inputs.compareTo(t.amount.add(t.relayFee).add(t.transactionFee)) >= 0)) return false; //not enough input
        ki.getMainLog().info("mark 4");
        if(inputs.subtract(t.amount.add(t.relayFee).add(t.transactionFee)).compareTo(t.change) != 0) return false; //CAN YOU NOT PERFORM SIMPLE MATH YOU FUCKING FAG-O-TRON
        ki.getMainLog().info("mark 5");
        if(!ki.getEncryptMan().verifySig(t.all(),t.signature,t.sender)) return false; //NOT YOUR TRANSACTION BROSKI
        ki.getMainLog().info("mark 6");
        if(!ki.getEncryptMan().verifySig(t.preSigAll(),t.relaySignature,t.relayer)) return false; //WHO THE FUCK RELAYED THIS
        ki.getMainLog().info("mark 7");

        return true;
    }

    //SOME RANDOM SHIT BELOW==========================
    /*
        for(String s:t.inputs.keySet())
        {
            if(t.inputs.get(s) == null)
            {
                  if(!ki.getChainMan().getByID(s).solver.equalsIgnoreCase(t.sender)) return false; // YOU DIDN'T SOLVE THIS BLOCK RETARD
                inputs = inputs.add(ChainManager.blockRewardForHeight(ki.getChainMan().getByID(s).height));
            }else {
                if (!ki.getChainMan().getByID(s).getTransaction(t.inputs.get(s).ID).receiver.equalsIgnoreCase(t.sender))
                    return false; //THAT'S NOT YOUR FUCKING TRANSACTION YOU THIEVING ASSHAT
                inputs = inputs.add(ki.getChainMan().getByID(s).getTransaction(t.inputs.get(s).ID).amount);
            }
        }
        */
    /*
            BigInteger lowestHeight = BigInteger.valueOf(0);

            for(String s:t.inputs.keySet()) {

                    if (ki.getChainMan().getByID(s).height.compareTo(lowestHeight) < 0) {
                        lowestHeight = ki.getChainMan().getByID(s).height;
                    }

            }
            //TODO: store public key to transactions per block so we can pull out only relevant transactions
            while(lowestHeight.compareTo(ki.getChainMan().currentHeight()) <= 0)
            {
                for(MKiTransaction trans: ki.getChainMan().getByHeight(lowestHeight).transactions.values())
                {
                    for(String s:t.inputs.keySet())
                    {
                        if(t.inputs.get(s) != null) {
                            if (t.inputs.get(s).ID.equalsIgnoreCase(trans.ID))
                                return false; //GET OUT OF HERE YOU DOUBLE SPENDING JEWISH ASSHOLE
                        }else{
                            if(trans.inputs.containsKey(s) && trans.inputs.get(s) == null)
                            {
                                return false; //ALREADY SPENT THAT BLOCK DUMBASS
                            }
                        }



                    }
                }
                lowestHeight = lowestHeight.add(BigInteger.ONE);
            }
            */
}
