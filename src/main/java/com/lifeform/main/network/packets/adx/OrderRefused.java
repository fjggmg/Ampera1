package com.lifeform.main.network.packets.adx;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.network.packets.Packet;
import com.lifeform.main.network.packets.PacketGlobal;

import java.io.Serializable;

public class OrderRefused implements Packet, Serializable {
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
