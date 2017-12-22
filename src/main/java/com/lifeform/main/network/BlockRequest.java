package com.lifeform.main.network;

import com.lifeform.main.IKi;

import java.io.Serializable;
import java.math.BigInteger;

public class BlockRequest implements Serializable, Packet {
    public BigInteger fromHeight;
    public boolean lite = false;
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        ki.debug("Received block request");
        if (!lite) {
            if (fromHeight.compareTo(ki.getChainMan().currentHeight()) < 0)
                pg.sendBlock(fromHeight.add(BigInteger.ONE));
        } else {
            pg.sendBlock(ki.getChainMan().currentHeight());
        }

    }

    @Override
    public int packetType() {
        return PacketType.BR.getIndex();
    }
}
