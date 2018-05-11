package com.lifeform.main.network.packets;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;

import java.io.Serializable;
import java.math.BigInteger;

public class BlockRequest implements Serializable, Packet {
    private static final long serialVersionUID = 184L;
    public BigInteger fromHeight;
    public boolean lite = false;
    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if(ki.getOptions().pDebug)
        ki.debug("Received block request");
        if (!lite) {
            if (fromHeight.compareTo(ki.getChainMan().currentHeight()) < 0)
                pg.sendBlock(fromHeight.add(BigInteger.ONE));
        } else {
            pg.sendBlock(ki.getChainMan().currentHeight());
        }

    }

}
