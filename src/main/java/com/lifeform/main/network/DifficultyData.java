package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.ChainManagerLite;

import java.io.Serializable;
import java.math.BigInteger;

public class DifficultyData implements Serializable, Packet {

    BigInteger difficulty;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if (ki.getOptions().lite) {
            ((ChainManagerLite) ki.getChainMan()).setDifficulty(difficulty);
        }
    }

    @Override
    public int packetType() {
        return 0;
    }
}
