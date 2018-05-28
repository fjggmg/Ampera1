package com.ampex.main.network.packets.adx;

import amp.HeadlessPrefixedAmplet;
import com.ampex.main.IKi;
import com.ampex.main.data.utils.InvalidAmpBuildException;
import com.ampex.main.network.IConnectionManager;
import com.ampex.main.network.packets.Packet;
import com.ampex.main.network.packets.PacketGlobal;

import java.math.BigInteger;
import java.nio.charset.Charset;

public class OrderReduced implements Packet {
    public String ID;
    public BigInteger amount;
    public String transaction;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if (ID != null && amount != null && transaction != null)
            ki.getExMan().reduceOrder(ID, amount, transaction);
        if (ki.getNetMan().isRelay()) {
            ki.getNetMan().broadcastAllBut(connMan.getID(), this);
        }
    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {
        try {
            HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(serialized);
            ID = new String(hpa.getNextElement(), Charset.forName("UTF-8"));
            amount = new BigInteger(hpa.getNextElement());
            transaction = new String(hpa.getNextElement(), Charset.forName("UTF-8"));
        } catch (Exception e) {
            throw new InvalidAmpBuildException("Unable to create OrderReduced from bytes");
        }
    }

    @Override
    public byte[] serializeToBytes() {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        hpa.addElement(ID);
        hpa.addElement(amount);
        hpa.addElement(transaction);
        return hpa.serializeToBytes();
    }
}
