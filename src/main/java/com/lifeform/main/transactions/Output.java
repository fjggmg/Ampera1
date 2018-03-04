package com.lifeform.main.transactions;

import amp.Amplet;
import amp.classification.AmpClassCollection;
import amp.classification.classes.AC_SingleElement;
import amp.group_primitives.UnpackedGroup;
import amp.serialization.IAmpAmpletSerializable;
import com.lifeform.main.data.AmpIDs;
import com.lifeform.main.data.EncryptionManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Created by Bryan on 8/8/2017.
 */
public class Output implements TXIO, IAmpAmpletSerializable {

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
    @SuppressWarnings("unchecked")
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

    @Override
    public Amplet serializeToAmplet() {
        AC_SingleElement amount = AC_SingleElement.create(AmpIDs.AMOUNT_GID, this.amount.toByteArray());
        AC_SingleElement receiver = AC_SingleElement.create(AmpIDs.RECEIVER_GID, this.receiver.toByteArray());
        AC_SingleElement token = AC_SingleElement.create(AmpIDs.TOKEN_GID, this.token.toString());
        AC_SingleElement index = AC_SingleElement.create(AmpIDs.INDEX_GID, this.index);
        AC_SingleElement timestamp = AC_SingleElement.create(AmpIDs.TXTIMESTAMP_GID, this.timestamp);
        AmpClassCollection acc = new AmpClassCollection();
        acc.addClass(amount);
        acc.addClass(receiver);
        acc.addClass(token);
        acc.addClass(index);
        acc.addClass(timestamp);
        Amplet amp = acc.serializeToAmplet();
        return amp;
    }

    public static Output fromAmp(Amplet amp) {
        UnpackedGroup ag = amp.unpackGroup(AmpIDs.AMOUNT_GID);
        UnpackedGroup rg = amp.unpackGroup(AmpIDs.RECEIVER_GID);
        UnpackedGroup tg = amp.unpackGroup(AmpIDs.TOKEN_GID);
        UnpackedGroup ig = amp.unpackGroup(AmpIDs.INDEX_GID);
        UnpackedGroup tmg = amp.unpackGroup(AmpIDs.TXTIMESTAMP_GID);
        BigInteger amount = new BigInteger(ag.getElement(0));
        Address receiver = Address.fromByteArray(rg.getElement(0));
        Token token = Token.valueOf(tg.getElementAsString(0));
        int index = ig.getElementAsInt(0);
        long timestamp = tmg.getElementAsLong(0);
        return new Output(amount, receiver, token, index, timestamp);

    }
}
