package com.lifeform.main.network;

import com.lifeform.main.IKi;

import java.io.Serializable;

public class UTXODataStart implements Packet,Serializable {
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        ki.resetLite();
        UTXOStartAck usa = new UTXOStartAck();
        usa.addresses = ki.getAddMan().getAll();
        connMan.sendPacket(usa);//USA! USA! USA!
    }

    @Override
    public int packetType() {
        return 0;
    }
}
