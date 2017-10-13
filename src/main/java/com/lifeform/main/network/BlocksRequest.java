package com.lifeform.main.network;

import com.lifeform.main.IKi;

import java.io.Serializable;
import java.math.BigInteger;

public class BlocksRequest implements Serializable, Packet {
    BigInteger fromHeight;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        ki.debug("Received blocks request");
        if (fromHeight.compareTo(ki.getChainMan().currentHeight()) <= 0)
            pg.sendFromHeight(fromHeight);
        else {
            LastAgreedStart las = new LastAgreedStart();
            las.height = ki.getChainMan().currentHeight();
            pg.laFlag = true;
            connMan.sendPacket(las);
        }

    }

    @Override
    public int packetType() {
        return PacketType.BR.getIndex();
    }
}
