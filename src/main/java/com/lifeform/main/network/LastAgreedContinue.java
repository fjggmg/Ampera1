package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.Block;

import java.io.Serializable;
import java.math.BigInteger;

public class LastAgreedContinue implements Serializable, Packet {
    BigInteger height;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if(ki.getOptions().pDebug)
        ki.debug("received last agreed continue");
        BlockHeader bh;
        Block b = ki.getChainMan().getByHeight(height);
        bh = pg.formHeader(b);
        bh.laFlag = true;
        connMan.sendPacket(bh);
    }

    @Override
    public int packetType() {
        return PacketType.LAC.getIndex();
    }
}
