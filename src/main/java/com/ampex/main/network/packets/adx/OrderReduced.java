package com.ampex.main.network.packets.adx;

import amp.HeadlessPrefixedAmplet;
import com.ampex.amperabase.IConnectionManager;
import com.ampex.amperabase.IKiAPI;
import com.ampex.amperabase.InvalidAmpBuildException;
import com.ampex.amperanet.packets.Packet;
import com.ampex.amperanet.packets.PacketGlobal;
import com.ampex.main.IKi;

import java.math.BigInteger;
import java.nio.charset.Charset;

public class OrderReduced implements Packet {
    public String ID;
    public BigInteger amount;
    public String transaction;

    @Override
    public void process(IKiAPI ki, IConnectionManager connMan, PacketGlobal pg) {
        if (ID != null && amount != null && transaction != null)
            ((IKi) ki).getExMan().reduceOrder(ID, amount, transaction);
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
        } catch (RuntimeException re) {
            throw re;
        }catch (Exception e) {
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
