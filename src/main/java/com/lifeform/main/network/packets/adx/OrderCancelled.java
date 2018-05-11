package com.lifeform.main.network.packets.adx;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.network.packets.Packet;
import com.lifeform.main.network.packets.PacketGlobal;

import java.io.Serializable;

public class OrderCancelled implements Packet, Serializable {
    public String ID;
    public byte[] sig;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        ki.getExMan().cancelRequest(ID, sig);
        if (ki.getNetMan().isRelay()) {
            ki.getNetMan().broadcastAllBut(connMan.getID(), this);
        }
    }

}
