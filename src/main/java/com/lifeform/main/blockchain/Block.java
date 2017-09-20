package com.lifeform.main.blockchain;


import com.lifeform.main.Ki;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.JSONManager;
import com.lifeform.main.data.Utils;
import com.lifeform.main.transactions.ITrans;
import com.lifeform.main.transactions.Transaction;
import org.json.simple.JSONObject;

import java.math.BigInteger;
import java.util.*;

/**
 * Created by Bryan on 5/28/2017.
 *
 *
 * Data bucket with some simple methods to grab shit and concat it
 */
public class Block {

    public BigInteger height;
    public String prevID;
    public String ID;
    public String solver;
    public Long timestamp = 0L;
    /**
     * the following is a way to store data in the block, although said data may be arbitrary.
     */
    public String payload = "";

    private ITrans coinbase;

    public void setCoinbase(ITrans coinbase)
    {
        this.coinbase = coinbase;
    }

    public ITrans getCoinbase()
    {
        return coinbase;
    }

    private Map<String,ITrans> transactions = new HashMap<>();

    public void addTransaction(ITrans trans)
    {
        merkleRoot = null;
        transactions.put(trans.getID(),trans);
    }

    public ITrans getTransaction(String ID)
    {
        return transactions.get(ID);
    }

    public final Set<String> getTransactionKeys()
    {
        return transactions.keySet();
    }

    public void addAll(Map<String,ITrans> transes)
    {
        merkleRoot = null;
        for(String trans:transes.keySet())
        {
            System.out.println("Adding transaction: " + trans);
            transactions.put(trans,transes.get(trans));
        }
    }

    public void remove(String transaction)
    {
        merkleRoot = null;
        transactions.remove(transaction);
    }
    public String merkleRoot;

    public String merkleRoot()
    {
        List<String> hashes = new ArrayList<>();

        hashes.addAll(transactions.keySet());
        List<String> temp = new ArrayList<>();
        if(hashes.size() == 0)
        {
            merkleRoot = EncryptionManager.sha512(height.toString());
            return merkleRoot;
        }
        while(hashes.size() > 1)
        {

            for(int i = 0; i < hashes.size(); i = i + 2)
            {
                if(i == hashes.size() - 1)
                {
                    temp.add(EncryptionManager.sha512(hashes.get(i) + hashes.get(i)));
                }else {
                    temp.add(EncryptionManager.sha512(hashes.get(i) + hashes.get(i + 1)));
                }

            }
            hashes.clear();
            hashes.addAll(temp);
            temp.clear();
        }
        merkleRoot = hashes.get(0);
        return hashes.get(0);
    }
    private String payloadHash;
    private String solverHash;
    private String header;
    private String coinbaseID;
    private String sHeight;
    public String header()
    {
        payloadHash = EncryptionManager.sha512(payload);
        if(solverHash == null || solverHash.isEmpty())
        {
            solverHash = EncryptionManager.sha512(solver);
        }
        if(coinbaseID == null || coinbaseID.isEmpty())
        {
            coinbaseID = coinbase.getID();
        }

        sHeight = Utils.toBase64(height.toByteArray());


        header = prevID + solverHash + sHeight + timestamp + payloadHash + coinbaseID + ((merkleRoot == null) ? merkleRoot():merkleRoot);

        return header;
    }

    public String toJSON()
    {
        JSONObject obj = new JSONObject();
        obj.put("prevID",prevID);
        obj.put("height", Utils.toBase64(height.toByteArray()));
        obj.put("ID",ID);
        obj.put("solver",solver);
        obj.put("timestamp",timestamp.toString());
        obj.put("payload",payload);
        JSONObject obj2 = new JSONObject();
        for(String trans:transactions.keySet())
        {
            obj2.put(trans,transactions.get(trans).toJSON());
        }
        obj.put("transactions",obj2.toJSONString());
        obj.put("coinbase",coinbase.toJSON());
        return obj.toJSONString();
    }

    public static Block fromJSON(String json)
    {
        Map<String,String> map = JSONManager.parseJSONtoMap(json);
        Block b = new Block();
        b.prevID = map.get("prevID");
        b.height = new BigInteger(Utils.fromBase64(map.get("height")));
        b.ID = map.get("ID");
        b.solver = map.get("solver");
        b.timestamp = Long.parseLong(map.get("timestamp"));
        b.payload = map.get("payload");
        b.setCoinbase(Transaction.fromJSON(map.get("coinbase")));
        Map<String,String> transactions = JSONManager.parseJSONtoMap(map.get("transactions"));
        for(String trans:transactions.keySet())
        {
            b.transactions.put(trans, Transaction.fromJSON(transactions.get(trans)));
        }
        return b;
    }

}
