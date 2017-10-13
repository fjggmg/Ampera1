package com.lifeform.main.network;

import com.lifeform.main.IKi;

import java.io.Serializable;

public class BadBlockEnd implements Serializable, Packet {
    String ID;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        pg.bMap.remove(pg.headerMap.get(ID));
        pg.headerMap.remove(ID);
        pg.onRightChain = false;
    }

    @Override
    public int packetType() {
        return PacketType.BBE.getIndex();
    }
}
