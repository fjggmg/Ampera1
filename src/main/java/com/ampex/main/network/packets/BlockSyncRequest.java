package com.ampex.main.network.packets;

import com.ampex.main.IKi;
import com.ampex.main.data.utils.InvalidAmpBuildException;
import com.ampex.main.network.IConnectionManager;

import java.math.BigInteger;

public class BlockSyncRequest implements Packet {

    public BigInteger height;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if (height == null) return;
        if (height.compareTo(ki.getChainMan().currentHeight()) < 0) {
            pg.sendBlock(height.add(BigInteger.ONE));
        }
    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {
        try {
            height = new BigInteger(serialized);
        } catch (Exception e) {
            throw new InvalidAmpBuildException("Unable to create BlockSyncRequest from bytes");
        }
    }

    @Override
    public byte[] serializeToBytes() {
        if (height != null)
            return height.toByteArray();
        else return new byte[0];
    }
}
