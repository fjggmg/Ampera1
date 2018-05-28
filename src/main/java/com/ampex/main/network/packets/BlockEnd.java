package com.ampex.main.network.packets;

import com.ampex.main.IKi;
import com.ampex.main.blockchain.Block;
import com.ampex.main.data.utils.InvalidAmpBuildException;
import com.ampex.main.network.IConnectionManager;
import com.ampex.main.transactions.ITrans;

import java.nio.charset.Charset;
import java.util.List;

public class BlockEnd implements Packet {

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

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {
        try {
            ID = new String(serialized, Charset.forName("UTF-8"));
        } catch (Exception e) {
            throw new InvalidAmpBuildException("Unable to create BLockEnd from bytes");
        }
    }

    @Override
    public byte[] serializeToBytes() {
        return ID.getBytes(Charset.forName("UTF-8"));
    }
}
