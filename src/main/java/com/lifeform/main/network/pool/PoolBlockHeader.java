package com.lifeform.main.network.pool;

import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.network.BlockEnd;
import com.lifeform.main.network.BlockHeader;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.network.TransactionPacket;
import com.lifeform.main.transactions.ITrans;
import com.lifeform.main.transactions.Transaction;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

public class PoolBlockHeader implements Serializable, PoolPacket {
    public String solver;
    public String merkleRoot;
    public String ID;
    public BigInteger height;
    public long timestamp;
    public String prevID;
    public byte[] payload;
    public String coinbase;

    @Override
    public void process(IKi ki, IConnectionManager connMan) {
        ki.debug("Received pool block header");
        Block b = new Block();
        if (!ki.getOptions().poolRelay) {
            b.solver = solver;
            b.merkleRoot = merkleRoot;
            b.ID = ID;
            b.height = height;
            b.timestamp = timestamp;
            b.prevID = prevID;
            try {
                b.payload = (ki.getPoolData().payTo + ki.getPoolData().ID).getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            b.setCoinbase(Transaction.fromJSON(coinbase));
            ki.getPoolData().blockData = b.gpuHeader();
            ki.getPoolData().currentWork = this;
        } else {
            if (ki.getPoolData().workMap.keySet().contains(b.merkleRoot)) {
                byte[] payload = b.payload;
                b = ki.getPoolData().workMap.get(b.merkleRoot);
                b.payload = payload;
                if (ki.getChainMan().softVerifyBlock(b).success()) {
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
                }

                ki.getPoolManager().addShare(b);
            }
        }

    }

    BlockHeader formHeader(Block b) {
        BlockHeader bh = new BlockHeader();
        bh.timestamp = b.timestamp;
        bh.solver = b.solver;
        bh.prevID = b.prevID;
        bh.payload = b.payload;
        bh.merkleRoot = b.merkleRoot();
        bh.ID = b.ID;
        bh.height = b.height;
        bh.coinbase = b.getCoinbase().toJSON();
        return bh;
    }
}