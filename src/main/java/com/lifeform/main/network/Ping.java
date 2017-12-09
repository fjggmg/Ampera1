package com.lifeform.main.network;

import com.lifeform.main.IKi;

import java.io.Serializable;

public class Ping implements Serializable, Packet {
    public long currentTime;
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        Pong pong = new Pong();
        pong.latency = System.currentTimeMillis() - currentTime;
        connMan.sendPacket(pong);
    }

    @Override
    public int packetType() {
        return 0;
    }
}
