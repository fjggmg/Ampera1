package com.lifeform.main.network.packets;

import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.transactions.ITrans;

import java.io.Serializable;
import java.util.List;

public class BlockEnd implements Serializable, Packet {
    public String ID;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        if(ki.getOptions().pDebug)
        ki.debug("Received block end");
        BlockHeader bh = pg.headerMap.get(ID);
        List<ITrans> trans = pg.bMap.get(bh);
        Block block = pg.formBlock(bh);
        if (block == null) {
            ki.debug("Something fucked up, block is null");
            return;
        }
        ki.debug("Block formed, adding transactions:");
        int i = 0;
        for (ITrans t : trans) {
            i++;
            ki.debug("Transaction " + i + " added");
            block.addTransaction(t);
        }
        ki.getStateManager().addBlock(block, connMan.getID());

        BlockAck ba = new BlockAck();
        ba.height = block.height;
        ba.verified = true;
        connMan.sendPacket(ba);


    }

}
