package com.lifeform.main.blockchain;

import com.lifeform.main.IKi;
import com.lifeform.main.network.BlockEnd;
import com.lifeform.main.network.BlockHeader;
import com.lifeform.main.network.TransactionPacket;

import java.math.BigInteger;
import java.util.*;

public class StateManager extends Thread implements IStateManager {
    private volatile Map<String, Map<BigInteger, Block>> connBlocks = new HashMap<>();
    private volatile boolean changed = false;
    private IKi ki;

    public StateManager(IKi ki) {
        this.ki = ki;
    }

    @Override
    public void addBlock(Block block, String connID) {
        if (connBlocks.get(connID) != null) {
            connBlocks.get(connID).put(block.height, block);
        } else {
            Map<BigInteger, Block> map = new HashMap<>();
            map.put(block.height, block);
            connBlocks.put(connID, map);
        }

        changed = true;
    }

    public void run() {
        while (true) {
            if (changed) {
                for (String connID : connBlocks.keySet()) {
                    if (connBlocks.get(connID).get(ki.getChainMan().currentHeight().add(BigInteger.ONE)) != null) {
                        if (ki.getChainMan().addBlock(connBlocks.get(connID).get(ki.getChainMan().currentHeight().add(BigInteger.ONE))).success()) {

                        }
                    }
                }


            }


            try {
                sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendBlock(Block b) {
        if (ki.getOptions().mDebug)
            ki.debug("Sending block to network from miner");
        BlockHeader bh2 = formHeader(b);
        ki.getNetMan().broadcast(bh2);


        for (String key : b.getTransactionKeys()) {
            TransactionPacket tp = new TransactionPacket();
            tp.block = b.ID;
            tp.trans = b.getTransaction(key).toJSON();
            ki.getNetMan().broadcast(tp);
        }
        BlockEnd be = new BlockEnd();
        be.ID = b.ID;
        ki.getNetMan().broadcast(be);
        if (ki.getOptions().mDebug)
            ki.debug("Done sending block");
    }

    private BlockHeader formHeader(Block b) {
        BlockHeader bh = new BlockHeader();
        bh.timestamp = b.timestamp;
        bh.solver = b.solver;
        bh.prevID = b.prevID;
        bh.payload = b.payload;
        bh.merkleRoot = b.merkleRoot;
        bh.ID = b.ID;
        bh.height = b.height;
        bh.coinbase = b.getCoinbase().toJSON();
        return bh;
    }


}
