package com.lifeform.main.blockchain;


import amp.Amplet;
import amp.classification.AmpClassCollection;
import amp.classification.classes.AC_ClassInstanceIDIsIndex;
import amp.classification.classes.AC_SingleElement;
import amp.group_primitives.UnpackedGroup;
import amp.serialization.IAmpAmpletSerializable;
import com.lifeform.main.data.AmpIDs;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.JSONManager;
import com.lifeform.main.data.Utils;
import com.lifeform.main.transactions.ITrans;
import com.lifeform.main.transactions.InvalidTransactionException;
import com.lifeform.main.transactions.Transaction;
import org.json.simple.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by Bryan on 5/28/2017.
 *
 *
 * Data bucket with some simple methods to grab shit and concat it
 */
public class Block implements IAmpAmpletSerializable {

    public BigInteger height;
    public String prevID;
    public String ID;
    public String solver;
    public Long timestamp = 0L;
    /**
     * the following is a way to store data in the block, although said data may be arbitrary.
     */
    public byte[] payload = {};

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
        for (Map.Entry<String, ITrans> trans : transes.entrySet())
        {
            //System.out.println("Adding transaction: " + trans);
            transactions.put(trans.getKey(), trans.getValue());
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

    private String solverHash;
    private String header;
    private String coinbaseID;
    private String sHeight;
    public String header()
    {
        if (payload.length > 256) return null;//limit on payload
        if (solver.length() > 1024) return null;//limit on solver (essentially payload 2)
        solverHash = EncryptionManager.sha512(solver);
        coinbaseID = coinbase.getID();
        sHeight = Utils.toBase64(height.toByteArray());


        try {
            header = prevID + solverHash + sHeight + timestamp + coinbaseID + ((merkleRoot == null) ? merkleRoot() : merkleRoot) + (((payload == null) || (payload.length == 0)) ? "" : new String(payload, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            //payload did not convert to string
            e.printStackTrace();
            return null;
        }

        return header;
    }

    /**
     * no payload header for gpu mining
     *
     * @return byte array header without the payload so the GPU miner can set the payload itself
     */
    public byte[] gpuHeader() {
        if (solverHash == null || solverHash.isEmpty()) {
            solverHash = EncryptionManager.sha512(solver);
        }
        if (coinbaseID == null || coinbaseID.isEmpty()) {
            coinbaseID = coinbase.getID();
        }
        sHeight = Utils.toBase64(height.toByteArray());


        header = prevID + solverHash + sHeight + timestamp + coinbaseID + ((merkleRoot == null) ? merkleRoot() : merkleRoot);


        try {
            return header.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
    public String toJSON()
    {
        JSONObject obj = new JSONObject();
        obj.put("prevID",prevID);
        obj.put("height", Utils.toBase64(height.toByteArray()));
        obj.put("ID",ID);
        obj.put("solver",solver);
        obj.put("timestamp",timestamp.toString());
        obj.put("payload", Utils.toBase64(payload));
        JSONObject obj2 = new JSONObject();
        for (Map.Entry<String, ITrans> trans : transactions.entrySet())
        {
            obj2.put(trans.getKey(), trans.getValue().toJSON());
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
        b.payload = Utils.fromBase64(map.get("payload"));
        try {
            b.setCoinbase(Transaction.fromJSON(map.get("coinbase")));
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
        }
        Map<String,String> transactions = JSONManager.parseJSONtoMap(map.get("transactions"));
        for (Map.Entry<String, String> trans : transactions.entrySet())
        {
            try {
                b.transactions.put(trans.getKey(), Transaction.fromJSON(trans.getValue()));
            } catch (InvalidTransactionException e) {
                e.printStackTrace();
            }
        }
        return b;
    }

    @Override
    public Amplet serializeToAmplet() {
        AC_SingleElement ID = AC_SingleElement.create(AmpIDs.BLOCK_ID_GID, Utils.fromBase64(this.ID));

        AC_SingleElement prevID;
        if (height.compareTo(BigInteger.ZERO) == 0) {

            prevID = AC_SingleElement.create(AmpIDs.PREV_ID_GID, this.prevID);

        } else {
            prevID = AC_SingleElement.create(AmpIDs.PREV_ID_GID, Utils.fromBase64(this.prevID));
        }
        AC_SingleElement height = AC_SingleElement.create(AmpIDs.HEIGHT_GID, this.height.toByteArray());
        AC_SingleElement solver = AC_SingleElement.create(AmpIDs.SOLVER_GID, Utils.fromBase64(this.solver));
        AC_SingleElement timestamp = AC_SingleElement.create(AmpIDs.TIMESTAMP_GID, this.timestamp);
        AC_SingleElement payload = null;
        if (this.payload != null && this.payload.length != 0)
            payload = AC_SingleElement.create(AmpIDs.PAYLOAD_GID, this.payload);
        AC_SingleElement coinbase = AC_SingleElement.create(AmpIDs.COINBASE_GID, this.coinbase);
        AC_ClassInstanceIDIsIndex transactions = AC_ClassInstanceIDIsIndex.create(AmpIDs.TRANSACTIONS_CID, "Transactions");

        for (Map.Entry<String, ITrans> tid : this.transactions.entrySet()) {
            transactions.addElement(tid.getValue());
        }
        AmpClassCollection acc = new AmpClassCollection();
        acc.addClass(ID);
        acc.addClass(prevID);
        acc.addClass(height);
        acc.addClass(solver);
        acc.addClass(timestamp);
        if (payload != null)
            acc.addClass(payload);
        acc.addClass(coinbase);
        acc.addClass(transactions);

        return acc.serializeToAmplet();
    }

    public static Block fromAmplet(Amplet amp) {

        String ID = Utils.toBase64(amp.unpackGroup(AmpIDs.BLOCK_ID_GID).getElement(0));
        BigInteger height = new BigInteger(amp.unpackGroup(AmpIDs.HEIGHT_GID).getElement(0));
        String prevID;
        if (height.compareTo(BigInteger.ZERO) == 0) {
            prevID = amp.unpackGroup(AmpIDs.PREV_ID_GID).getElementAsString(0);

        } else {
            prevID = Utils.toBase64(amp.unpackGroup(AmpIDs.PREV_ID_GID).getElement(0));
        }
        String solver = Utils.toBase64(amp.unpackGroup(AmpIDs.SOLVER_GID).getElement(0));
        long timestamp = amp.unpackGroup(AmpIDs.TIMESTAMP_GID).getElementAsLong(0);
        byte[] payload = null;
        if (amp.unpackGroup(AmpIDs.PAYLOAD_GID) != null)
            payload = amp.unpackGroup(AmpIDs.PAYLOAD_GID).getElement(0);
        ITrans coinbase = null;
        try {
            coinbase = Transaction.fromAmplet(amp.unpackGroup(AmpIDs.COINBASE_GID).getElementAsAmplet(0));
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
        }

        Map<String, ITrans> transactions = new HashMap<>();
        if (amp.unpackClass(AmpIDs.TRANSACTIONS_CID) != null)
            for (UnpackedGroup p : amp.unpackClass(AmpIDs.TRANSACTIONS_CID)) {
                ITrans t = null;
                try {
                    t = Transaction.fromAmplet(p.getElementAsAmplet(0));
                } catch (InvalidTransactionException e) {
                    e.printStackTrace();
                }
                if (t == null) return null;
                transactions.put(t.getID(), t);
            }
        Block b = new Block();
        b.ID = ID;
        b.prevID = prevID;
        b.height = height;
        b.solver = solver;
        b.timestamp = timestamp;
        if (payload != null)
            b.payload = payload;
        b.setCoinbase(coinbase);
        b.addAll(transactions);

        return b;

    }
}
