package com.lifeform.main.network.adx;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.network.Packet;
import com.lifeform.main.network.PacketGlobal;

import java.io.Serializable;

public class OrdersRequest implements Packet, Serializable {
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        for (String o : ki.getExMan().getOrderIDs()) {
            OrderPacket op = new OrderPacket();
            op.transaction = ki.getExMan().getOrder(o).getTxid();
            op.order = ki.getExMan().getOrder(o).serializeToBytes();
            op.matched = false;
            connMan.sendPacket(op);
        }
    }

    @Override
    public int packetType() {
        return 0;
    }
}
