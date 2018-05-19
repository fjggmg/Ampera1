package com.ampex.main.network.packets;

import com.ampex.main.IKi;
import com.ampex.main.network.IConnectionManager;

import java.io.Serializable;

public class Pong implements Serializable, Packet {
    private static final long serialVersionUID = 184L;
    long latency;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {

        connMan.setCurrentLatency(System.currentTimeMillis() - latency);
        ki.getNetMan().setLive(true);

    }

}
