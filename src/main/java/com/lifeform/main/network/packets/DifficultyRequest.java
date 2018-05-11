package com.lifeform.main.network.packets;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;

import java.io.Serializable;

public class DifficultyRequest implements Serializable, Packet {
    private static final long serialVersionUID = 184L;
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        DifficultyData dd = new DifficultyData();
        dd.difficulty = ki.getChainMan().getCurrentDifficulty();
        connMan.sendPacket(dd);
    }

}
