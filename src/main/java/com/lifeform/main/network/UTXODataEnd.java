package com.lifeform.main.network;

import com.lifeform.main.IKi;

import java.io.Serializable;

public class UTXODataEnd implements Packet,Serializable {
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {

    }

    @Override
    public int packetType() {
        return 0;
    }
}
