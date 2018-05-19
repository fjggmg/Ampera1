package com.ampex.main.network.packets;

import com.ampex.main.IKi;
import com.ampex.main.blockchain.ChainManagerLite;
import com.ampex.main.network.IConnectionManager;

import java.io.Serializable;
import java.math.BigInteger;

public class DifficultyData implements Serializable, Packet {
    private static final long serialVersionUID = 184L;
    BigInteger difficulty;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if (ki.getOptions().lite) {
            ((ChainManagerLite) ki.getChainMan()).setDifficulty(difficulty);
            ki.getNetMan().diffSet();
        }
    }

}
