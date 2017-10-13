package com.lifeform.main.network;

import com.lifeform.main.IKi;

import java.io.Serializable;
import java.math.BigInteger;

public class LastAgreedEnd implements Serializable, Packet {
    BigInteger height;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        ki.debug("Received last agreed end");
        ChainUpStart cus = new ChainUpStart();
        cus.startHeight = height.add(BigInteger.ONE);
        connMan.sendPacket(cus);
        pg.sendFromHeight(cus.startHeight);
        ChainUpEnd cue = new ChainUpEnd();
        cue.startHeight = cus.startHeight;
        connMan.sendPacket(cue);
    }

    @Override
    public int packetType() {
        return PacketType.LAE.getIndex();
    }
}
