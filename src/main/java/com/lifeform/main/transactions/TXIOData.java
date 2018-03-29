package com.lifeform.main.transactions;

import amp.ByteTools;
import amp.HeadlessPrefixedAmplet;
import amp.serialization.IAmpByteSerializable;
import com.lifeform.main.blockchain.IChainMan;
import com.lifeform.main.data.Utils;

import java.math.BigInteger;

public class TXIOData implements IAmpByteSerializable {
    private IAddress address;

    public IAddress getAddress() {
        return address;
    }


    public int getIndex() {
        return index;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public Token getToken() {
        return token;
    }

    public long getTimestamp() {
        return timestamp;
    }

    private int index;
    private BigInteger amount;
    private Token token;
    private long timestamp;

    public TXIOData(IAddress address, int index, BigInteger amount, Token token, long timestamp) throws InvalidTXIOData {
        if (!address.isValid()) throw new InvalidTXIOData("Invalid address");
        this.address = address;
        if (index > IChainMan.MAX_TXIOS) throw new InvalidTXIOData("Invalid index");
        this.index = index;
        if (amount.compareTo(BigInteger.ZERO) < 0) throw new InvalidTXIOData("Invalid height");
        this.amount = amount;
        this.token = token;
        this.timestamp = timestamp;
    }

    @Override
    public byte[] serializeToBytes() {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        hpa.addBytes(address.toByteArray());
        hpa.addBytes(ByteTools.deconstructInt(index));
        hpa.addElement(amount);
        hpa.addBytes(ByteTools.deconstructInt(token.getID()));
        hpa.addBytes(ByteTools.deconstructLong(timestamp));
        return hpa.serializeToBytes();
    }


    public static TXIOData fromByteArray(byte[] array) {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(array);
        IAddress add = Address.fromByteArray(hpa.getNextElement());
        int index = hpa.getNextElementAsHeadlessAmplet().getNextInt();
        BigInteger amount = new BigInteger(hpa.getNextElement());
        int token = hpa.getNextElementAsHeadlessAmplet().getNextInt();
        long timestamp = hpa.getNextElementAsHeadlessAmplet().getNextLong();
        try {
            return new TXIOData(add, index, amount, Token.byID(token), timestamp);
        } catch (InvalidTXIOData invalidTXIOData) {
            invalidTXIOData.printStackTrace();
            return null;
        }
    }
}
