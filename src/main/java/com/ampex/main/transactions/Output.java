package com.ampex.main.transactions;

import amp.Amplet;
import amp.ByteTools;
import amp.HeadlessAmplet;
import amp.HeadlessPrefixedAmplet;
import amp.classification.AmpClassCollection;
import amp.classification.classes.AC_SingleElement;
import amp.group_primitives.UnpackedGroup;
import com.ampex.amperabase.*;
import com.ampex.main.Ki;
import com.ampex.main.data.encryption.EncryptionManager;
import com.ampex.main.data.utils.Utils;
import com.ampex.main.transactions.addresses.Address;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.math.BigInteger;

/**
 * Created by Bryan on 8/8/2017.
 */
public class Output implements IOutput {
    public static final byte VERSION = 2;

    public Output(BigInteger amount, IAddress receiver, Token token, int index, long timestamp, byte version)
    {
        this.amount = amount;
        this.receiver = receiver;
        this.token = token;
        this.index = index;
        this.timestamp = timestamp;
        this.version = version;
    }
    private final int index;
    private final BigInteger amount;
    private final IAddress receiver;
    private final Token token;
    private final long timestamp;
    private final byte version;

    @Override
    public byte getVersion() {
        return version;
    }
    @Override
    public String getID()
    {
        //System.out.println("Getting ID of output of version: " + version);
        if (version != 2)
            return EncryptionManager.sha256(toJSON());
        else
            return Utils.toBase64(EncryptionManager.sha3256(serializeToBytes()));
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

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
        return index;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String toJSON() {
        //System.out.println("Calling toJSON on output");
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
            IAddress receiver = Address.decodeFromChain((String) jo.get("receiver"));
            Token token = Token.valueOf((String)jo.get("token"));
            long timestamp = Long.parseLong((String)jo.get("timestamp"));
            /*
            if (token == null) {
                Ki.getInstance().getMainLog().error("Token pulled from map is null, value on map: " + jo.get("token"));
                return null;
            }
            */
            return new Output(amount, receiver, token, index, timestamp, (byte) 1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Deprecated
    public Amplet serializeToAmpletOld() {

        AC_SingleElement amount = AC_SingleElement.create(AmpIDs.AMOUNT_GID, this.amount.toByteArray());
        AC_SingleElement receiver = AC_SingleElement.create(AmpIDs.RECEIVER_GID, this.receiver.toByteArray());
        AC_SingleElement token = null;

        token = AC_SingleElement.create(AmpIDs.TOKEN_GID, this.token.toString());

        AC_SingleElement index = AC_SingleElement.create(AmpIDs.INDEX_GID, this.index);
        AC_SingleElement timestamp = AC_SingleElement.create(AmpIDs.TXTIMESTAMP_GID, this.timestamp);
        AC_SingleElement version = AC_SingleElement.create(AmpIDs.TXIO_VER_GID, this.version);
        AmpClassCollection acc = new AmpClassCollection();
        acc.addClass(amount);
        acc.addClass(receiver);
        acc.addClass(token);
        acc.addClass(index);
        acc.addClass(timestamp);
        acc.addClass(version);
        Amplet amp = acc.serializeToAmplet();
        return amp;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TXIO && ((TXIO) o).getID().equals(getID());
    }

    @Override
    public int hashCode() {
        byte[] id = Utils.fromBase64(getID());
        return ByteTools.buildInt(id[0], id[1], id[2], id[3]);
    }

    @Deprecated
    public static IOutput fromAmp(Amplet amp) {

        UnpackedGroup ag = amp.unpackGroup(AmpIDs.AMOUNT_GID);
        UnpackedGroup rg = amp.unpackGroup(AmpIDs.RECEIVER_GID);
        UnpackedGroup tg = amp.unpackGroup(AmpIDs.TOKEN_GID);
        UnpackedGroup ig = amp.unpackGroup(AmpIDs.INDEX_GID);
        UnpackedGroup tmg = amp.unpackGroup(AmpIDs.TXTIMESTAMP_GID);
        UnpackedGroup ov = amp.unpackGroup(AmpIDs.TXIO_VER_GID);
        BigInteger amount = new BigInteger(ag.getElement(0));
        IAddress receiver = Address.fromByteArray(rg.getElement(0));
        Token token = null;

        token = Token.valueOf(tg.getElementAsString(0));

        int index = ig.getElementAsInt(0);
        long timestamp = tmg.getElementAsLong(0);
        byte version = 1;
        if (ov != null)
            version = ov.getElementAsByte(0);
        return new Output(amount, receiver, token, index, timestamp, version);

    }

    public static IOutput fromBytes(byte[] array) {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(array);
        BigInteger amount = new BigInteger(hpa.getNextElement());
        IAddress receiver = Address.fromByteArray(hpa.getNextElement());
        Token token;

        byte[] bytes = hpa.getNextElement();
        token = Token.byID(ByteTools.buildInt(bytes[0], bytes[1], bytes[2], bytes[3]));

        HeadlessAmplet hamplet = hpa.getNextElementAsHeadlessAmplet();
        int index = hamplet.getNextInt();
        long timestamp = hamplet.getNextLong();
        byte version = hamplet.getNextByte();
        return new Output(amount, receiver, token, index, timestamp, version);

    }

    @Override
    public byte[] serializeToBytes() {
        HeadlessAmplet hamplet = HeadlessAmplet.create();
        hamplet.addElement(index);
        hamplet.addElement(timestamp);
        hamplet.addElement(version);
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        hpa.addElement(amount);
        hpa.addBytes(receiver.toByteArray());
        if (token == null) {
            Ki.getInstance().getMainLog().fatal("TOKEN IN OUTPUT WAS NULL");
            return new byte[0];
        }
        hpa.addBytes(ByteTools.deconstructInt(token.getID()));

        hpa.addElement(hamplet);
        return hpa.serializeToBytes();
    }
}
