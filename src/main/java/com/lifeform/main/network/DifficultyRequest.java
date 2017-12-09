package com.lifeform.main.network;

import com.lifeform.main.IKi;

import java.io.Serializable;

public class DifficultyRequest implements Serializable, Packet {
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        DifficultyData dd = new DifficultyData();
        dd.difficulty = ki.getChainMan().getCurrentDifficulty();
        connMan.sendPacket(dd);
    }

    @Override
    public int packetType() {
        return 0;
    }
}
