package com.ampex.main.network.packets;

import com.ampex.main.IKi;
import com.ampex.main.network.IConnectionManager;

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
