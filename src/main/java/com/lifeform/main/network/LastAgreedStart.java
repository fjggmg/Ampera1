package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.Block;

import java.io.Serializable;
import java.math.BigInteger;

public class LastAgreedStart implements Serializable, Packet {
    public BigInteger height;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if(ki.getOptions().pDebug)
        ki.debug("Received last agreed start");
        /*
        if (pg.onRightChain) {
            ResetRequest rr = new ResetRequest();
            rr.proof = pg.formHeader(ki.getChainMan().getByHeight(height));
            connMan.sendPacket(rr);
            return;
        }
        */
        //reset request is not working correctly right now, we need to figure this out
        BlockHeader bh;
        Block b = ki.getChainMan().getByHeight(height);
        bh = pg.formHeader(b);
        bh.laFlag = true;
        connMan.sendPacket(bh);
    }

    @Override
    public int packetType() {
        return PacketType.LAS.getIndex();
    }
}
