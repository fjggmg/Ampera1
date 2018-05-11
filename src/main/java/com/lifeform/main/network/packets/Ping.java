package com.lifeform.main.network.packets;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;

import java.io.Serializable;

public class Ping implements Serializable, Packet {
    private static final long serialVersionUID = 184L;
    public long currentTime;
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        Pong pong = new Pong();
        pong.latency = currentTime;
        connMan.sendPacket(pong);
    }

}
