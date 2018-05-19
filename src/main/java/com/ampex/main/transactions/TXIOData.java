package com.ampex.main.transactions;

import amp.ByteTools;
import amp.HeadlessPrefixedAmplet;
import amp.serialization.IAmpByteSerializable;
import com.ampex.amperabase.IAddress;
import com.ampex.amperabase.Token;
import com.ampex.main.blockchain.IChainMan;
import com.ampex.main.transactions.addresses.Address;

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

    public byte getVersion() {
        return version;
    }
    private int index;
    private BigInteger amount;
    private Token token;
    private long timestamp;
    private byte version;

    public TXIOData(IAddress address, int index, BigInteger amount, Token token, long timestamp, byte version) throws InvalidTXIOData {
        //we're special casing this, the old address system fucked up and failed us, unfortunately that leaves
        //this a bit vulnerable, but what the fuck ever, after the old address lockout this won't cause any harm
        if (!address.isValid() && address.getVersion() != Address.VERSION)
            throw new InvalidTXIOData("Invalid address: " + address.encodeForChain());
        this.address = address;
        if (index > IChainMan.MAX_TXIOS) throw new InvalidTXIOData("Invalid index");
        this.index = index;
        if (amount.compareTo(BigInteger.ZERO) < 0) throw new InvalidTXIOData("Invalid height");
        this.amount = amount;
        this.token = token;
        this.timestamp = timestamp;
        this.version = version;
    }

    @Override
    public byte[] serializeToBytes() {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        hpa.addBytes(address.toByteArray());
        hpa.addBytes(ByteTools.deconstructInt(index));
        hpa.addElement(amount);
        hpa.addBytes(ByteTools.deconstructInt(token.getID()));
        hpa.addBytes(ByteTools.deconstructLong(timestamp));
        hpa.addElement(version);
        return hpa.serializeToBytes();
    }


    public static TXIOData fromByteArray(byte[] array) {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(array);
        IAddress add = Address.fromByteArray(hpa.getNextElement());
        int index = hpa.getNextElementAsHeadlessAmplet().getNextInt();
        BigInteger amount = new BigInteger(hpa.getNextElement());
        int token = hpa.getNextElementAsHeadlessAmplet().getNextInt();
        long timestamp = hpa.getNextElementAsHeadlessAmplet().getNextLong();
        byte version = hpa.getNextElement()[0];
        try {
            return new TXIOData(add, index, amount, Token.byID(token), timestamp, version);
        } catch (InvalidTXIOData invalidTXIOData) {
            invalidTXIOData.printStackTrace();
            return null;
        }
    }
}
