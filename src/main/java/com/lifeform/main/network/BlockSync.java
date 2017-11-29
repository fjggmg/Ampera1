package com.lifeform.main.network;

import com.lifeform.main.IKi;

import java.io.Serializable;
import java.math.BigInteger;

public class BlockSync implements Serializable, Packet {

    BigInteger height;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if (height.compareTo(ki.getChainMan().currentHeight()) > 0) {
            BlockRequest br = new BlockRequest();
            br.fromHeight = ki.getChainMan().currentHeight();
            connMan.sendPacket(br);
        } else if (height.compareTo(ki.getChainMan().currentHeight()) < 0) {
            pg.sendFromHeight(height);
        }

    }

    @Override
    public int packetType() {
        return 0;
    }
}
