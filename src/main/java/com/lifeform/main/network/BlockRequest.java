package com.lifeform.main.network;

import com.lifeform.main.IKi;

import java.io.Serializable;
import java.math.BigInteger;

public class BlockRequest implements Serializable, Packet {
    BigInteger fromHeight;
    boolean lite = false;
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        ki.debug("Received block request");
        if (!lite) {
            if (fromHeight.compareTo(ki.getChainMan().currentHeight()) <= 0)
                pg.sendBlock(fromHeight.add(BigInteger.ONE));
            else {
                LastAgreedStart las = new LastAgreedStart();
                las.height = ki.getChainMan().currentHeight();
                pg.laFlag = true;
                connMan.sendPacket(las);
            }
        } else {
            pg.sendBlock(ki.getChainMan().currentHeight().subtract(BigInteger.valueOf(30L)));
        }

    }

    @Override
    public int packetType() {
        return PacketType.BR.getIndex();
    }
}
