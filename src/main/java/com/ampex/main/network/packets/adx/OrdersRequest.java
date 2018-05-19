package com.ampex.main.network.packets.adx;

import com.ampex.main.IKi;
import com.ampex.main.network.IConnectionManager;
import com.ampex.main.network.packets.Packet;
import com.ampex.main.network.packets.PacketGlobal;

import java.io.Serializable;

public class OrdersRequest implements Packet, Serializable {
    private static final long serialVersionUID = 184L;
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

}
