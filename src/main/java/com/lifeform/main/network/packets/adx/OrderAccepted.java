package com.lifeform.main.network.packets.adx;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.network.packets.Packet;
import com.lifeform.main.network.packets.PacketGlobal;

import java.io.Serializable;

public class OrderAccepted implements Packet, Serializable {
    private static final long serialVersionUID = 184L;
    public String ID;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        ki.getExMan().orderAccepted(ID);
    }

}
