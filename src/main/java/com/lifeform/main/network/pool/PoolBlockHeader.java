package com.lifeform.main.network.pool;

import amp.Amplet;
import com.lifeform.main.IKi;
import com.lifeform.main.Settings;
import com.lifeform.main.StringSettings;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.blockchain.ChainManager;
import com.lifeform.main.data.Utils;
import com.lifeform.main.network.BlockEnd;
import com.lifeform.main.network.BlockHeader;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.network.TransactionPacket;
import com.lifeform.main.transactions.*;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class PoolBlockHeader implements Serializable, PoolPacket {
    public String solver;
    public String merkleRoot;
    public String ID;
    public BigInteger height;
    public long timestamp;
    public String prevID;
    public byte[] payload;
    public byte[] coinbase;
    public long currentHR;
    public boolean pplns = false;
    @Override
    public void process(IKi ki, IConnectionManager connMan) {

        ki.debug("Received pool block header");
        Block b = new Block();
        if (!ki.getOptions().poolRelay) {

            solver = Utils.toBase64(ki.getPoolData().payTo.toByteArray());
            b.solver = Utils.toBase64(ki.getPoolData().payTo.toByteArray());
            b.merkleRoot = merkleRoot;
            ki.debug("merkle root: " + merkleRoot);
            b.ID = ID;
            b.height = height;
            ki.debug("height: " + height);
            b.timestamp = timestamp;
            ki.debug("timestamp: " + timestamp);
            b.prevID = prevID;

            try {
                payload = (ki.getPoolData().ID).getBytes("UTF-8");
                b.payload = (ki.getPoolData().ID).getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            b.setCoinbase(NewTrans.fromAmplet(Amplet.create(coinbase)));

            ki.getPoolData().blockData = b.gpuHeader();
            ki.debug("Setting as current work");
            ki.getPoolData().currentWork = this;
        } else {
            //ki.debug("Merkle root is: " + merkleRoot);

            if (ki.getPoolData().workMap.keySet().contains(merkleRoot)) {
                //ki.debug("Adding share to share list");
                ki.debug("height: " + height);
                //ki.debug("timestamp: " + timestamp);
                ki.debug("ID: " + ID);
                //ki.debug("====================================End of block data==========================");
                //if (ki.getPoolData().workMap == null) ki.debug("workmap null");
                //else ki.debug("workmap not null");
                //ki.debug("contents of workmap keyset:");
                //for (String mr : ki.getPoolData().workMap.keySet()) {
                //   ki.debug(mr);
                //}
                //ki.debug("Block ID in workmap:");
                //ki.debug(ki.getPoolData().workMap.get(merkleRoot).ID);

                if (Block.fromAmplet(ki.getPoolData().workMap.get(merkleRoot).serializeToAmplet()) == null)
                    ki.debug("Block did not copy correctly");
                b = Block.fromAmplet(ki.getPoolData().workMap.get(merkleRoot).serializeToAmplet());

                b.payload = payload;
                b.timestamp = timestamp;
                b.ID = ID;
                b.solver = solver;
                ki.getPoolData().hrMap.put(connMan.getID(), currentHR);
                if (ChainManager.checkSolve(ki.getChainMan().getCurrentDifficulty(), b.height, b.ID)) {
                    if (ki.getChainMan().softVerifyBlock(b).success()) {
                        ki.debug("Share is a solve");
                        BlockHeader bh2 = formHeader(b);
                        ki.getNetMan().broadcast(bh2);
                        for (String key : b.getTransactionKeys()) {
                            TransactionPacket tp = new TransactionPacket();
                            tp.block = b.ID;
                            tp.trans = b.getTransaction(key).serializeToAmplet().serializeToBytes();
                            ki.getNetMan().broadcast(tp);
                        }
                        BlockEnd be = new BlockEnd();
                        be.ID = b.ID;
                        ki.getNetMan().broadcast(be);
                        if (pplns && ki.getSetting(Settings.PPLNS_SERVER)) {
                            List<Block> bs = new ArrayList<>();
                            bs.add(b);
                            ki.getPoolManager().endPPLNSRound(bs, Double.parseDouble(ki.getStringSetting(StringSettings.POOL_FEE)) / 100, ki);
                        }
                    }
                }
                if (pplns) {
                    if (ki.getSetting(Settings.PPLNS_SERVER))
                        ki.getPoolManager().addPPLNSShare(b);
                } else {
                    ki.getPoolManager().addShare(b);
                }
                //ki.debug("Shares for address:  " + ki.getPoolManager().getTotalSharesOfMiner(Address.decodeFromChain(ki.getPoolData().addMap.get(connMan.getID()))));
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
        bh.coinbase = b.getCoinbase().serializeToAmplet().serializeToBytes();
        return bh;
    }
}
