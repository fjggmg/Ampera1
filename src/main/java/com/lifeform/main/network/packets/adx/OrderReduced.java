package com.lifeform.main.network.packets.adx;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.network.packets.Packet;
import com.lifeform.main.network.packets.PacketGlobal;

import java.io.Serializable;
import java.math.BigInteger;

public class OrderReduced implements Packet, Serializable {
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

}
