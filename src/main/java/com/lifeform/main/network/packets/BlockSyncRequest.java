package com.lifeform.main.network.packets;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;

import java.io.Serializable;
import java.math.BigInteger;

public class BlockSyncRequest implements Packet, Serializable {
    public BigInteger height;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if (height.compareTo(ki.getChainMan().currentHeight()) < 0) {
            pg.sendBlock(height.add(BigInteger.ONE));
        }
    }

}
