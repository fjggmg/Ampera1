package com.lifeform.main.network;

import com.lifeform.main.IKi;

import java.io.Serializable;

public class Ping implements Serializable, Packet {
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        connMan.sendPacket(new Pong());
    }

    @Override
    public int packetType() {
        return 0;
    }
}
