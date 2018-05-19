package com.ampex.main.network.packets.adx;

import com.ampex.main.IKi;
import com.ampex.main.network.IConnectionManager;
import com.ampex.main.network.packets.Packet;
import com.ampex.main.network.packets.PacketGlobal;

import java.io.Serializable;

public class OrderAccepted implements Packet, Serializable {
    private static final long serialVersionUID = 184L;
    public String ID;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        ki.getExMan().orderAccepted(ID);
    }

}
