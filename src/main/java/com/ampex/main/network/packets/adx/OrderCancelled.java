package com.ampex.main.network.packets.adx;

import amp.HeadlessPrefixedAmplet;
import com.ampex.amperabase.IConnectionManager;
import com.ampex.amperabase.IKiAPI;
import com.ampex.amperabase.InvalidAmpBuildException;
import com.ampex.amperanet.packets.Packet;
import com.ampex.amperanet.packets.PacketGlobal;
import com.ampex.main.IKi;

import java.nio.charset.Charset;

public class OrderCancelled implements Packet {
    public String ID;
    public byte[] sig;

    @Override
    public void process(IKiAPI ki, IConnectionManager connMan, PacketGlobal pg) {
        ((IKi) ki).getExMan().cancelRequest(ID, sig);
        if (ki.getNetMan().isRelay()) {
            ki.getNetMan().broadcastAllBut(connMan.getID(), this);
        }
    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {
        try {
            HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(serialized);
            ID = new String(hpa.getNextElement(), Charset.forName("UTF-8"));
            sig = hpa.getNextElement();
        } catch (Exception e) {
            throw new InvalidAmpBuildException("Unable to create OrderCancelled from bytes");
        }
    }

    @Override
    public byte[] serializeToBytes() {
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        hpa.addElement(ID);
        hpa.addBytes(sig);
        return hpa.serializeToBytes();
    }
}
