package com.ampex.main.network.packets.adx;

import com.ampex.amperabase.IConnectionManager;
import com.ampex.amperabase.IKiAPI;
import com.ampex.amperabase.InvalidAmpBuildException;
import com.ampex.amperanet.packets.Packet;
import com.ampex.amperanet.packets.PacketGlobal;
import com.ampex.main.IKi;

import java.nio.charset.Charset;

public class OrderMatched implements Packet {
    public String ID;

    @Override
    public void process(IKiAPI ki, IConnectionManager connMan, PacketGlobal pg) {
        ((IKi) ki).getExMan().matchOrder(ID);
        if (ki.getNetMan().isRelay()) {
            ki.getNetMan().broadcastAllBut(connMan.getID(), this);
        }
    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {
        try {
            ID = new String(serialized, Charset.forName("UTF-8"));
        } catch (Exception e) {
            throw new InvalidAmpBuildException("Unable to create OrderMatched from bytes");
        }
    }

    @Override
    public byte[] serializeToBytes() {
        if (ID != null)
            return ID.getBytes(Charset.forName("UTF-8"));
        else
            return new byte[0];
    }
}
