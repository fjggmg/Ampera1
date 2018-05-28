package com.ampex.main.network.packets;

import com.ampex.main.IKi;
import com.ampex.main.blockchain.ChainManagerLite;
import com.ampex.main.data.utils.InvalidAmpBuildException;
import com.ampex.main.network.IConnectionManager;

import java.math.BigInteger;

public class DifficultyData implements Packet {

    BigInteger difficulty;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if (ki.getOptions().lite) {
            ((ChainManagerLite) ki.getChainMan()).setDifficulty(difficulty);
            ki.getNetMan().diffSet();
        }
    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {
        try {
            difficulty = new BigInteger(serialized);
        } catch (Exception e) {
            throw new InvalidAmpBuildException("Unable to create DifficultyData from bytes");
        }
    }

    @Override
    public byte[] serializeToBytes() {
        if (difficulty != null)
            return difficulty.toByteArray();
        else
            return new byte[0];
    }
}
