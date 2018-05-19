package com.ampex.main.network.packets;

import com.ampex.main.IKi;
import com.ampex.main.network.IConnectionManager;

import java.io.Serializable;
import java.math.BigInteger;

public class PackagedBlocksRequest implements Packet, Serializable {
    private static final long serialVersionUID = 184L;
    public BigInteger fromBlock;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {

        if (fromBlock == null) return;
        PackagedBlocks pb = PackagedBlocks.createPackage(ki, fromBlock);
        connMan.sendPacket(pb);
    }
}
