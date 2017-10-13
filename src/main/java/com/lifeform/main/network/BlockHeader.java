package com.lifeform.main.network;

import com.lifeform.main.IKi;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;

public class BlockHeader implements Serializable, Packet {
    public String solver;
    public String merkleRoot;
    public String ID;
    public BigInteger height;
    public long timestamp;
    public String prevID;
    public byte[] payload;
    public String coinbase;
    public boolean laFlag = false;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        ki.debug("Received block header");
        ki.debug("Height: " + height);
        pg.headerMap.put(ID, this);
        if (pg.laFlag && laFlag) {
            if (ki.getChainMan().getByHeight(height).ID.equals(ID)) {
                LastAgreedEnd lae = new LastAgreedEnd();
                lae.height = height;
                laFlag = false;
                connMan.sendPacket(lae);
            } else {
                LastAgreedContinue lac = new LastAgreedContinue();
                lac.height = height.subtract(BigInteger.ONE);
                connMan.sendPacket(lac);
            }
        } else if (pg.cuFlag) {
            pg.cuMap.put(this, new ArrayList<>());
        } else {
            pg.bMap.put(this, new ArrayList<>());
            if (ki.getNetMan().isRelay()) {
                ki.getNetMan().broadcast(this);
            }
        }
    }

    @Override
    public int packetType() {
        return PacketType.BH.getIndex();
    }
}
