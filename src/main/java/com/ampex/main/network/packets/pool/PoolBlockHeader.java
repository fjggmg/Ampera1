package com.ampex.main.network.packets.pool;

import amp.HeadlessAmplet;
import amp.HeadlessPrefixedAmplet;
import com.ampex.amperabase.IBlockAPI;
import com.ampex.amperabase.IConnectionManager;
import com.ampex.amperabase.InvalidAmpBuildException;
import com.ampex.amperabase.TransactionFeeCalculator;
import com.ampex.amperanet.packets.BlockEnd;
import com.ampex.amperanet.packets.BlockHeader;
import com.ampex.amperanet.packets.TransactionPacket;
import com.ampex.main.IKi;
import com.ampex.main.Settings;
import com.ampex.main.StringSettings;
import com.ampex.main.blockchain.Block;
import com.ampex.main.blockchain.ChainManager;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class PoolBlockHeader implements PoolPacket {

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
        Block b;// = new Block();
        if (!ki.getOptions().poolRelay) {
            /*
            solver = utils.toBase64(ki.getPoolData().payTo.toByteArray());
            b.solver = utils.toBase64(ki.getPoolData().payTo.toByteArray());
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
            */
            //ki.getPoolData().blockData = b.gpuHeader();
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
                            List<IBlockAPI> bs = new ArrayList<>();
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
            } else {
                if (ki.getPoolData().workMap.containsKey(ki.getPoolData().currentWork.merkleRoot))
                    connMan.sendPacket(ki.getPoolData().currentWork);
                else {
                    b = ki.getChainMan().formEmptyBlock(TransactionFeeCalculator.MIN_FEE);
                    PoolBlockHeader pbh = new PoolBlockHeader();
                    pbh.coinbase = b.getCoinbase().serializeToAmplet().serializeToBytes();
                    pbh.height = b.height;
                    pbh.ID = b.ID;
                    pbh.merkleRoot = b.merkleRoot();
                    pbh.prevID = b.prevID;
                    pbh.solver = b.solver;
                    pbh.timestamp = b.timestamp;
                    ki.getPoolData().workMap.put(b.merkleRoot(), b);
                    BigInteger height = ki.getPoolData().lowestHeight;
                    while (height.compareTo(b.height) != 0) {
                        if (ki.getPoolData().tracking.get(height) != null)
                            for (String root : ki.getPoolData().tracking.get(height)) {
                                ki.getPoolData().workMap.remove(root);
                            }
                        ki.getPoolData().tracking.remove(height);

                        height = height.add(BigInteger.ONE);
                    }
                    ki.getPoolData().lowestHeight = b.height;
                    ki.getPoolData().currentWork = pbh;
                    ki.getPoolNet().broadcast(pbh);
                }
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

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {
        try {
            HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create(serialized);
            HeadlessAmplet ha = hpa.getNextElementAsHeadlessAmplet();
            timestamp = ha.getNextLong();
            pplns = ha.getNextBoolean();
            currentHR = ha.getNextLong();
            solver = new String(hpa.getNextElement(), Charset.forName("UTF-8"));
            merkleRoot = new String(hpa.getNextElement(), Charset.forName("UTF-8"));
            ID = new String(hpa.getNextElement(), Charset.forName("UTF-8"));
            height = new BigInteger(hpa.getNextElement());
            prevID = new String(hpa.getNextElement(), Charset.forName("UTF-8"));
            payload = hpa.getNextElement();
            coinbase = hpa.getNextElement();
        } catch(RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new InvalidAmpBuildException("Unable to create PoolBlockHeader from bytes");
        }
    }

    @Override
    public byte[] serializeToBytes() {
        HeadlessAmplet ha = HeadlessAmplet.create();
        ha.addElement(timestamp);
        ha.addElement(pplns);
        ha.addElement(currentHR);
        HeadlessPrefixedAmplet hpa = HeadlessPrefixedAmplet.create();
        hpa.addElement(ha);
        hpa.addElement(solver);
        hpa.addElement(merkleRoot);
        hpa.addElement(ID);
        hpa.addElement(height);
        hpa.addElement(prevID);
        hpa.addBytes(payload);
        hpa.addBytes(coinbase);
        return hpa.serializeToBytes();
    }
}
