package com.lifeform.main.network.adx;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.network.Packet;
import com.lifeform.main.network.PacketGlobal;

import java.io.Serializable;

public class OrderAccepted implements Packet, Serializable {
    public String ID;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        ki.getExMan().orderAccepted(ID);
    }

    @Override
    public int packetType() {
        return 0;
    }
}
