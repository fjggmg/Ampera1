package com.lifeform.main.network;

import com.lifeform.main.IKi;

import java.io.Serializable;
import java.util.List;

public class RelayList implements Serializable, Packet {
    List<String> relays;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        pg.relays = relays;
        if (ki.getNetMan().getConnections().size() < 4) {
            for (String IP : relays) {
                ki.getNetMan().attemptConnect(IP);
            }
        }
    }

    @Override
    public int packetType() {
        return PacketType.RL.getIndex();
    }
}
