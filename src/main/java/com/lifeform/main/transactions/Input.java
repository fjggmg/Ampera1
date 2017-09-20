package com.lifeform.main.transactions;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.math.BigInteger;

/**
 * Created by Bryan on 8/9/2017.
 */
public class Input implements TXIO{

    public Input(String prevID,int prevOut,BigInteger amount,Address receiver,Token token,long timestamp)
    {
        this.prevID = prevID;
        this.prevOut = prevOut;
        this.amount = amount;
        this.receiver = receiver;
        this.token = token;
        this.timestamp = timestamp;
    }

    private long timestamp;
    public String getID()
    {
        return prevID;
    }
    public boolean canSpend(String keys, String entropy)
    {
        return getAddress().canSpend(keys,entropy);
    }
    private String prevID;
    private int prevOut;
    private BigInteger amount;
    private Address receiver;
    private Token token;
    @Override
    public Address getAddress() {
        return receiver;
    }

    @Override
    public BigInteger getAmount() {
        return amount;
    }

    @Override
    public Token getToken() {
        return token;
    }

    @Override
    public int getIndex() {
        return prevOut;
    }

    @Override
    public String toJSON()
    {
        JSONObject jo = new JSONObject();
        jo.put("prevID",prevID);
        jo.put("prevOut",Integer.toString(prevOut));
        jo.put("amount",amount.toString());
        jo.put("receiver",receiver.encodeForChain());
        jo.put("token",token.toString());
        jo.put("timestamp",timestamp);
        return jo.toJSONString();
    }

    public static Input fromJSONString(String json)
    {
        try {
            JSONObject jo = (JSONObject) (new JSONParser().parse(json));
            String prevID = (String)jo.get("prevID");
            int prevOut = Integer.parseInt((String)jo.get("prevOut"));
            BigInteger amount = new BigInteger((String)jo.get("amount"));
            Address receiver = Address.decodeFromChain((String)jo.get("receiver"));
            Token token = Token.valueOf((String)jo.get("token"));
            long timestamp =(long) jo.get("timestamp");
            return new Input(prevID,prevOut,amount,receiver,token,timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getTimestamp()
    {
        return timestamp;
    }

    public static Input fromOutput(Output output)
    {
        return new Input(output.getID(),output.getIndex(),output.getAmount(),output.getAddress(),output.getToken(),output.getTimestamp());
    }
}
