package com.lifeform.main.network.adx;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.network.Packet;
import com.lifeform.main.network.PacketGlobal;

import java.io.Serializable;

public class OrderMatched implements Packet, Serializable {
    public String ID;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        ki.getExMan().matchOrder(ID);
        if (ki.getNetMan().isRelay()) {
            ki.getNetMan().broadcastAllBut(connMan.getID(), this);
        }
    }

    @Override
    public int packetType() {
        return 0;
    }
}
