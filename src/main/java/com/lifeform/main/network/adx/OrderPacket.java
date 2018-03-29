package com.lifeform.main.network.adx;

import amp.Amplet;
import com.lifeform.main.IKi;
import com.lifeform.main.adx.Order;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.network.Packet;
import com.lifeform.main.network.PacketGlobal;
import com.lifeform.main.transactions.ITrans;
import com.lifeform.main.transactions.NewTrans;

import java.io.Serializable;

public class OrderPacket implements Packet, Serializable {
    public byte[] order;
    public String transaction;
    public boolean matched = false;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if (ki.getOptions().pDebug)
            ki.debug("Received Order Packet");
        Order order = Order.fromByteArray(this.order);
        if (order == null) {
            ki.getMainLog().warn("Received order packet with null order");
            return;
        }
        if (matched) {
            ki.getExMan().addMatched(order);
            return;
        }
        if (transaction != null) {
            ki.getExMan().addPending(transaction, order);
            if (ki.getNetMan().isRelay()) {
                if (ki.getOptions().pDebug)
                    ki.debug("Broadcasting order packet");
                ki.getNetMan().broadcastAllBut(connMan.getID(), this);
            }
        }
    }

    @Override
    public int packetType() {
        return 0;
    }
}
