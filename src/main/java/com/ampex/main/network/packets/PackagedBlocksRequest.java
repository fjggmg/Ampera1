package com.ampex.main.network.packets;

import com.ampex.main.IKi;
import com.ampex.main.data.utils.InvalidAmpBuildException;
import com.ampex.main.network.IConnectionManager;

import java.math.BigInteger;

public class PackagedBlocksRequest implements Packet {

    public BigInteger fromBlock;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {

        if (fromBlock == null) return;
        PackagedBlocks pb = PackagedBlocks.createPackage(ki, fromBlock);
        connMan.sendPacket(pb);
    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {
        try {
            fromBlock = new BigInteger(serialized);
        } catch (Exception e) {
            throw new InvalidAmpBuildException("Unable to build PackagedBlocksRequest from bytes");
        }
    }

    @Override
    public byte[] serializeToBytes() {
        if (fromBlock != null)
            return fromBlock.toByteArray();
        else
            return new byte[0];
    }
}
