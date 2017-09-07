package com.lifeform.main.transactions;

import com.lifeform.main.data.EncryptionManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.math.BigInteger;

/**
 * Created by Bryan on 8/8/2017.
 */
public class Output implements TXIO{

    public Output(BigInteger amount, Address receiver,Token token, int index,long timestamp)
    {
        this.amount = amount;
        this.receiver = receiver;
        this.token = token;
        this.index = index;
        this.timestamp = timestamp;
    }
    private final int index;
    private final BigInteger amount;
    private final Address receiver;
    private final Token token;
    private final long timestamp;
    @Override
    public String getID()
    {
        return EncryptionManager.sha256(toJSON());
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

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
        return index;
    }

    @Override
    public String toJSON() {
        JSONObject jo = new JSONObject();
        jo.put("index",Integer.toString(index));
        jo.put("amount",amount.toString());
        jo.put("receiver",receiver.encodeForChain());
        jo.put("token",token.toString());
        jo.put("timestamp","" + timestamp);
        return jo.toJSONString();
    }


    public static Output fromJSON(String json)
    {
        try {
            JSONObject jo = (JSONObject) (new JSONParser().parse(json));
            int index = Integer.parseInt((String)jo.get("index"));
            BigInteger amount = new BigInteger((String)jo.get("amount"));
            Address receiver = Address.decodeFromChain((String)jo.get("receiver"));
            Token token = Token.valueOf((String)jo.get("token"));
            long timestamp = Long.parseLong((String)jo.get("timestamp"));
            return new Output(amount,receiver,token,index,timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
