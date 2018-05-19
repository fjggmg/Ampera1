package com.ampex.main.network.packets.adx;

import com.ampex.main.IKi;
import com.ampex.main.network.IConnectionManager;
import com.ampex.main.network.packets.Packet;
import com.ampex.main.network.packets.PacketGlobal;

import java.io.Serializable;

public class OrderRefused implements Packet, Serializable {
    private static final long serialVersionUID = 184L;
    String ID;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        ki.debug("Received OrderRefused packet for Order: " + ID);

        if (ki.getExMan().getOrder(ID) != null) {
            ki.getExMan().removeOrder(ID);
            ki.getExMan().orderRejected(ID);
            OrdersRequest or = new OrdersRequest();
            connMan.sendPacket(or);
        }
    }

}
