package com.lifeform.main.transactions;

import com.lifeform.main.data.JSONManager;
import com.lifeform.main.data.Utils;
import org.json.simple.JSONObject;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bryan on 4/7/2017.
 */
public class MKiTransaction {

    /**
     * height when transaction was made for easier lookup of what segment it may be in
     */
    public BigInteger height;
    /**
     * signature before sending to relay by maker
     */
    public String preSig;
    /**
     * amount of the transaction
     */
    public BigInteger amount;
    /**
     * fee to pay to the block solver that verifies this transaction
     */
    public BigInteger transactionFee;
    /**
     * fee to pay to relayer who relays this transaction initially
     */
    public BigInteger relayFee;
    /**
     * signature of transaction maker after receiving response from relay accepting transaction
     */
    public String signature;
    /**
     * signature of relayer signed once received with good presig and relay fee amount
     */
    public String relaySignature;
    /**
     * public key of transaction maker
     */
    public String sender;
    /**
     * public key of transaction recipient
     */
    public String receiver;
    /**
     * public key of chosen relayer
     */
    public String relayer;

    /**
     * ID of this transaction (hash)
     */
    public String ID;

    /**
     * change for transaction
     */
    public BigInteger change;

    /**
     * all the shit that goes into this transaction to make it happen
     */
    public Map<String,MKiTransaction> inputs = new HashMap<>();


    public String all()
    {
        StringBuilder builder = new StringBuilder();
        for(String key:inputs.keySet())
        {
            builder.append(key);
        }
        return preSig + amount + transactionFee + relayFee + relaySignature + sender + receiver + relayer  + ID + change + builder.toString() + Utils.toHexArray(height.toByteArray());
    }


    public String preSigAll()
    {
        StringBuilder builder = new StringBuilder();
        for(String key:inputs.keySet())
        {
            builder.append(key);
        }
        return  "" + amount + transactionFee + relayFee + sender + receiver + relayer + change + builder.toString() + Utils.toHexArray(height.toByteArray());
    }


    public String toJSON()
    {
        JSONObject obj = new JSONObject();

        obj.put("preSig",preSig);
        obj.put("amount",amount.toString());
        obj.put("transactionFee",transactionFee.toString());
        obj.put("relayFee",relayFee.toString());
        obj.put("relaySignature",relaySignature);
        obj.put("sender",sender);
        obj.put("receiver",receiver);
        obj.put("relayer",relayer);
        obj.put("ID",ID);
        obj.put("change",change.toString());
        obj.put("signature",signature);
        JSONObject obj2 = new JSONObject();
        for(String trans:inputs.keySet())
        {
            if(inputs.get(trans) != null)
            obj2.put(trans,inputs.get(trans).ID);
            else
                obj2.put(trans,null);
        }
        obj.put("transactions",obj2.toJSONString());
        obj.put("height",height.toString());

        return obj.toJSONString();
    }

    public static MKiTransaction fromJSON(String json)
    {
        Map<String,String> map = JSONManager.parseJSONtoMap(json);

        MKiTransaction trans = new MKiTransaction();

        trans.preSig = map.get("preSig");
        trans.amount = new BigInteger(map.get("amount"));
        trans.transactionFee = new BigInteger(map.get("transactionFee"));
        trans.relayFee = new BigInteger(map.get("relayFee"));
        trans.relaySignature = map.get("relaySignature");
        trans.sender = map.get("sender");
        trans.receiver = map.get("receiver");
        trans.relayer = map.get("relayer");
        trans.ID = map.get("ID");
        trans.change = new BigInteger(map.get("change"));
        trans.signature = map.get("signature");
        Map<String,String> transactions = JSONManager.parseJSONtoMap(map.get("transactions"));

        for(String block:transactions.keySet())
        {
            MKiTransaction t = new MKiTransaction();
            if(!(transactions.get(block) == null) && !transactions.get(block).equalsIgnoreCase("null")) {
                t.ID = transactions.get(block);
                trans.inputs.put(block, t);
            }else{
                trans.inputs.put(block,null);
            }
        }
        trans.height = new BigInteger(map.get("height"));

        return trans;

    }

    /**
     * only call once transaction is fully setup with amount,relay fee, and transaction fee
     * @return change
     */
    public BigInteger calculateChange()
    {
        BigInteger c = BigInteger.ZERO;


        for(String ID:inputs.keySet())
        {

            c = c.add(inputs.get(ID).amount);
        }

        c = c.subtract(relayFee);
        c = c.subtract(transactionFee);
        c = c.subtract(amount);

        return c;
    }

}
