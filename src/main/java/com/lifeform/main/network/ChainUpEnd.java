package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.Block;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class ChainUpEnd implements Serializable, Packet {
    BigInteger startHeight;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        ki.debug("Received chain update end");
        pg.cuFlag = false;
        Map<BigInteger, Block> heightMap = new HashMap<>();
        BigInteger max = BigInteger.ZERO;
        for (Block b : pg.cuBlocks) {
            if (b.height.compareTo(max) > 0) max = b.height;
            heightMap.put(b.height, b);
        }
        ki.debug("Checking to make sure is forward moving update");
        if (max.compareTo(ki.getChainMan().currentHeight()) <= 0) {
            return;
        }
        BigInteger height = startHeight;

        for (; height.compareTo(max) <= 0; height = height.add(BigInteger.ONE)) {
            ki.debug("Verifying block with height: " + height + " before doing final commit");
            if (!pg.temp.addBlock(heightMap.get(height)).success()) {
                return;
            }
        }
        ki.debug("Undoing chain to height: " + startHeight);
        if (ki.getChainMan().currentHeight().compareTo(startHeight) >= 0)
            ki.getChainMan().undoToBlock(ki.getChainMan().getByHeight(startHeight).ID);
        height = startHeight;
        for (; height.compareTo(max) <= 0; height = height.add(BigInteger.ONE)) {
            ki.debug("Adding block with height: " + height + " to local files");
            if (!ki.getChainMan().addBlock(heightMap.get(height)).success()) {
                ki.getMainLog().info("Error updating chain to larger competing chain, chain unfinished, will attempt to pull updates for this chain");
                return;
            }
        }
        //redo connection so we can resync the chains
        BlockSync bs = new BlockSync();
        bs.height = ki.getChainMan().currentHeight();
        connMan.sendPacket(bs);
    }

    @Override
    public int packetType() {
        return PacketType.CUE.getIndex();
    }
}
