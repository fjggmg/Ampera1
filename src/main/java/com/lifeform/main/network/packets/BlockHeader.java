package com.lifeform.main.network.packets;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.concurrent.CopyOnWriteArrayList;

public class BlockHeader implements Serializable, Packet {
    public String solver;
    public String merkleRoot;
    public String ID;
    public BigInteger height;
    public long timestamp;
    public String prevID;
    public byte[] payload;
    public byte[] coinbase;
    public boolean laFlag = false;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if(ki.getOptions().pDebug)
        ki.debug("Received block header");
        if(ki.getOptions().pDebug)
        ki.debug("Height: " + height);

        pg.headerMap.put(ID, this);

        pg.bMap.put(this, new CopyOnWriteArrayList<>());


    }

}
