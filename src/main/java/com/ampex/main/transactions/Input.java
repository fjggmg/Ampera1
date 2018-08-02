package com.ampex.main.transactions;

import amp.Amplet;
import amp.ByteTools;
import amp.HeadlessAmplet;
import amp.HeadlessPrefixedAmplet;
import amp.classification.AmpClassCollection;
import amp.classification.classes.AC_SingleElement;
import amp.group_primitives.UnpackedGroup;
import com.ampex.amperabase.*;
import com.ampex.main.transactions.addresses.Address;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

/**
 * Created by Bryan on 8/9/2017.
 */
public class Input implements IInput {

    public static final byte VERSION = 2;

    public Input(String prevID, int prevOut, BigInteger amount, IAddress receiver, Token token, long timestamp, byte version)
    {
        this.prevID = prevID;
        this.prevOut = prevOut;
        this.amount = amount;
        this.receiver = receiver;
        this.token = token;
        this.timestamp = timestamp;
        this.version = version;
    }

    private byte version = 1;
    private long timestamp;

    @Override
    public String getID() {
        return prevID;
    }

    public boolean canSpend(String keys, String entropy, String prefix, boolean p2sh, KeyType keyType)
    {
        if (prefix == null || prefix.length() != 5)
            return getAddress().canSpend(keys, entropy, p2sh, keyType);
        else
            return getAddress().canSpendPrefixed(keys, entropy, prefix, p2sh, keyType);
    }
    private String prevID;
    private int prevOut;
    private BigInteger amount;
    private IAddress receiver;
    private Token token;
    @Override
    public IAddress getAddress() {
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
            IAddress receiver = Address.decodeFromChain((String) jo.get("receiver"));
            Token token = Token.valueOf((String)jo.get("token"));
            long timestamp =(long) jo.get("timestamp");
            return new Input(prevID, prevOut, amount, receiver, token, timestamp, (byte) 1);
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

    @Override
    public byte getVersion() {
        return version;
    }

    public static IInput fromOutput(IOutput output)
    {
        return new Input(output.getID(), output.getIndex(), output.getAmount(), output.getAddress(), output.getToken(), output.getTimestamp(), VERSION);
    }

    @Override
    public byte[] serializeToBytes() {
        HeadlessAmplet hamplet = HeadlessAmplet.create();
        hamplet.addElement(prevOut);
        hamplet.addElement(timestamp);
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        hpa.addElement(amount);
        hpa.addBytes(receiver.toByteArray());

        //System.out.println("serializing input to version: " + version);
        if (version == 2)
            hpa.addElement(token.getID());
        else {
            if (!token.equals(Token.ORIGIN))
                hpa.addElement(token.getName());
            else
                hpa.addElement("Origin");

        }
        hpa.addElement(prevID);

        hpa.addElement(hamplet);
        if(version != 1)
        hpa.addElement(version);
        return hpa.serializeToBytes();
    }

    public static Input fromBytes(byte[] array) {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(array);
        BigInteger amount = new BigInteger(hpa.getNextElement());
        IAddress receiver = Address.fromByteArray(hpa.getNextElement());
        Token token;
        String prevID;
        byte[] tokenBytes = hpa.getNextElement();
        try {
            prevID = new String(hpa.getNextElement(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        HeadlessAmplet hamplet = hpa.getNextElementAsHeadlessAmplet();
        int index = hamplet.getNextInt();
        long timestamp = hamplet.getNextLong();
        byte version = 1;
        if (hpa.hasNextElement()) {
            version = hpa.getNextElement()[0];
        }
        if (version == 2) {
            token = Token.byID(ByteTools.buildInt(tokenBytes[0], tokenBytes[1], tokenBytes[2], tokenBytes[3]));
        } else {

            try {
                String name = new String(tokenBytes, "UTF-8");
                if (name.equalsIgnoreCase("origin")) {
                    token = Token.ORIGIN;
                } else {
                    token = Token.byName(name);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        }
        //System.out.println("Input is version " + version);
        return new Input(prevID, index, amount, receiver, token, timestamp, version);
    }

    @Override
    public Amplet serializeToAmplet() {
        AC_SingleElement amount = AC_SingleElement.create(AmpIDs.AMOUNT_GID, this.amount.toByteArray());
        AC_SingleElement receiver = AC_SingleElement.create(AmpIDs.RECEIVER_GID, this.receiver.toByteArray());
        AC_SingleElement token = null;
        token = AC_SingleElement.create(AmpIDs.TOKEN_GID, this.token.toString());

        AC_SingleElement index = AC_SingleElement.create(AmpIDs.INDEX_GID, this.prevOut);
        AC_SingleElement timestamp = AC_SingleElement.create(AmpIDs.TXTIMESTAMP_GID, this.timestamp);
        AC_SingleElement ID = null;

        ID = AC_SingleElement.create(AmpIDs.ID_GID, this.getID());

        AmpClassCollection acc = new AmpClassCollection();
        acc.addClass(amount);
        acc.addClass(receiver);
        acc.addClass(token);
        acc.addClass(index);
        acc.addClass(timestamp);
        acc.addClass(ID);
        Amplet amp = acc.serializeToAmplet();
        return amp;
    }

    public static Input fromAmp(Amplet amp) {
        UnpackedGroup ag = amp.unpackGroup(AmpIDs.AMOUNT_GID);
        UnpackedGroup rg = amp.unpackGroup(AmpIDs.RECEIVER_GID);
        UnpackedGroup tg = amp.unpackGroup(AmpIDs.TOKEN_GID);
        UnpackedGroup ig = amp.unpackGroup(AmpIDs.INDEX_GID);
        UnpackedGroup idg = amp.unpackGroup(AmpIDs.ID_GID);
        UnpackedGroup tmg = amp.unpackGroup(AmpIDs.TXTIMESTAMP_GID);

        return new Input(idg.getElementAsString(0), ig.getElementAsInt(0), new BigInteger(ag.getElement(0)), Address.fromByteArray(rg.getElement(0)), Token.valueOf(tg.getElementAsString(0)), tmg.getElementAsLong(0), (byte) 1);

    }
}
