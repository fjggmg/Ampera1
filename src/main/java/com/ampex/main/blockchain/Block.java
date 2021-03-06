package com.ampex.main.blockchain;


import amp.Amplet;
import amp.classification.AmpClassCollection;
import amp.classification.classes.AC_ClassInstanceIDIsIndex;
import amp.classification.classes.AC_SingleElement;
import amp.group_primitives.UnpackedGroup;
import amp.serialization.IAmpAmpletSerializable;
import com.ampex.amperabase.AmpIDs;
import com.ampex.amperabase.IBlockAPI;
import com.ampex.amperabase.ITransAPI;
import com.ampex.amperabase.InvalidTransactionException;
import com.ampex.main.data.encryption.EncryptionManager;
import com.ampex.main.data.utils.JSONManager;
import com.ampex.main.data.utils.Utils;
import com.ampex.main.transactions.ITrans;
import com.ampex.main.transactions.Transaction;
import org.json.simple.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by Bryan on 5/28/2017.
 *
 *
 */
public class Block implements IAmpAmpletSerializable, IBlockAPI {

    public BigInteger height;
    public String prevID;
    public String ID;
    public String solver;
    public Long timestamp = 0L;

    public byte[] payload = {};

    private ITransAPI coinbase;

    public void setCoinbase(ITransAPI coinbase)
    {
        this.coinbase = coinbase;
    }

    @Override
    public ITransAPI getCoinbase() {
        return coinbase;
    }

    private Map<String, ITransAPI> transactions = new HashMap<>();

    public void addTransaction(ITransAPI trans)
    {
        merkleRoot = null;
        transactions.put(trans.getID(),trans);
    }

    @Override
    public ITransAPI getTransaction(String ID) {
        return transactions.get(ID);
    }

    @Override
    public final Set<String> getTransactionKeys() {
        return transactions.keySet();
    }

    public void addAll(Map<String, ITransAPI> transes)
    {
        merkleRoot = null;
        for (Map.Entry<String, ITransAPI> trans : transes.entrySet())
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

    @Override
    public BigInteger getHeight() {
        return height;
    }

    @Override
    public String getPrevID() {
        return prevID;
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String getSolver() {
        return solver;
    }

    @Override
    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public byte[] getPayload() {
        return Arrays.copyOf(payload, payload.length);
    }

    @Override
    public String getMerkleRoot() {
        return merkleRoot;
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
            return new byte[0];
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
        for (Map.Entry<String, ITransAPI> trans : transactions.entrySet())
        {
            obj2.put(trans.getKey(), ((ITrans) trans.getValue()).toJSON());
        }
        obj.put("transactions",obj2.toJSONString());
        obj.put("coinbase", ((ITrans) coinbase).toJSON());
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

        for (Map.Entry<String, ITransAPI> tid : this.transactions.entrySet()) {
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

        Map<String, ITransAPI> transactions = new HashMap<>();
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
