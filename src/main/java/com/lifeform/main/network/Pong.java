package com.lifeform.main.network;

import com.lifeform.main.IKi;

import java.io.Serializable;

public class Pong implements Serializable, Packet {
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        ki.getNetMan().setLive(true);
    }

    @Override
    public int packetType() {
        return 0;
    }
}
