package com.lifeform.main.network.packets;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;

import java.io.Serializable;

public class Pong implements Serializable, Packet {
    long latency;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {

        connMan.setCurrentLatency(System.currentTimeMillis() - latency);
        ki.getNetMan().setLive(true);

    }

}
