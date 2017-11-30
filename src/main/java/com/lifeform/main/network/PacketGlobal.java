package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.blockchain.ChainManager;
import com.lifeform.main.transactions.ITrans;
import com.lifeform.main.transactions.Transaction;
import org.eclipse.collections.impl.bimap.mutable.HashBiMap;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PacketGlobal {


    PacketGlobal(IKi ki, IConnectionManager connMan) {
        this.connMan = connMan;
        this.ki = ki;
    }

    List<Block> addedBlocks = new ArrayList<>();
    boolean doneDownloading = false;
    BigInteger startHeight;
    boolean laFlag = false;
    boolean onRightChain = true;
    Map<BlockHeader, List<ITrans>> bMap = new HashMap<>();
    Map<BlockHeader, List<ITrans>> cuMap = new HashMap<>();
    List<Block> cuBlocks = new ArrayList<>();
    List<Block> futureBlocks = new ArrayList<>();
    Map<String, BlockHeader> headerMap = new HashMap<>();
    ChainManager temp;
    IConnectionManager connMan;
    IKi ki;
    boolean gotPending = false;
    boolean cuFlag = false;
    List<String> relays;

    void sendFromHeight(BigInteger height) {
        for (; height.compareTo(ki.getChainMan().currentHeight()) <= 0; height = height.add(BigInteger.ONE)) {
            if (height.compareTo(BigInteger.valueOf(-1L)) != 0)
                sendBlock(height);
        }
        if (ki.getChainMan().getTemp() != null && ki.getChainMan().getTemp().height.compareTo(ki.getChainMan().currentHeight()) > 0) {
            sendBlock(ki.getChainMan().getTemp());
        }
    }

    void sendBlock(final Block b) {
        BlockHeader bh2 = formHeader(b);
        connMan.sendPacket(bh2);


        for (String key : b.getTransactionKeys()) {
            TransactionPacket tp = new TransactionPacket();
            tp.block = b.ID;
            tp.trans = b.getTransaction(key).toJSON();
            connMan.sendPacket(tp);
        }
        BlockEnd be = new BlockEnd();
        be.ID = b.ID;
        connMan.sendPacket(be);
        if (b.height.compareTo(ki.getChainMan().currentHeight()) == 0) {
            for (ITrans trans : ki.getTransMan().getPending()) {
                TransactionPacket tp = new TransactionPacket();
                tp.trans = trans.toJSON();
                connMan.sendPacket(tp);
            }
        }

        if (!resendMap.containsKey(b.height)) {
            resendTimesMap.merge(b.height, 1, (a, b1) -> a + b1);
            if (resendTimesMap.get(b.height) > 5) {
                ki.debug("Disconnecting connection since retry to send a single block has taken more than 5 tries");
                connMan.disconnect();
                return;
            }
            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(450000);
                } catch (InterruptedException e) {
                    //fail silently as we expect this to happen
                    if (ki.getOptions().pDebug)
                        ki.debug("Block resend #" + b.height.toString() + " interrupted");
                    return;
                }
                ki.debug("Did not receive BlockAck within 45 seconds, resending");
                sendBlock(b);
            });
            t.setName("Block #" + b.height.toString() + " Resend Thread");
            t.start();
            if (lastCancelled.compareTo(b.height) >= 0)
                t.interrupt();
            else
                resendMap.put(b.height, t);
        }
    }
    void sendBlock(final BigInteger height) {
        Block b = ki.getChainMan().getByHeight(height);
        sendBlock(b);
    }
    private BigInteger lastCancelled = BigInteger.ZERO;
    private Map<BigInteger,Integer> resendTimesMap = new HashMap<>();
    private Map<BigInteger,Thread> resendMap = new HashMap<>();
    public void cancelResend(BigInteger height)
    {
        if(ki.getOptions().pDebug)
        ki.debug("Cancelling resend #" + height.toString());
        if(resendMap.get(height) != null)
        resendMap.get(height).interrupt();

        resendMap.remove(height);
        resendTimesMap.remove(height);
    }

    Block formBlock(BlockHeader bh) {
        if (bh == null) {
            ki.debug("We don't have the block header for this block end, our connection to the network must be fucked");
            return null;
        }
        if (bh.prevID == null) {
            ki.debug("Malformed block header received. PrevID is null");
            return null;
        }
        if (bh.ID == null) {
            ki.debug("Malformed block header received. ID is null");
            return null;
        }
        if (bh.height == null) {
            ki.debug("Malformed block header received. height is null");
            return null;
        }
        if (bh.coinbase == null) {
            ki.debug("Malformed block header received. coinbase is null");
            return null;
        }
        if (bh.merkleRoot == null) {
            ki.debug("Malformed block header received. merkleroot is null");
            return null;
        }
        if (bh.payload == null) {
            ki.debug("Malformed block header received. payload is null");
            return null;
        }
        if (bh.solver == null) {
            ki.debug("Malformed block header received. solver is null");
            return null;
        }
        if (bh.timestamp == 0) {
            ki.debug("Malformed block header received. timestamp is impossible");
            return null;
        }
        Block block = new Block();
        block.height = bh.height;
        block.ID = bh.ID;
        block.merkleRoot = bh.merkleRoot;
        block.payload = bh.payload;
        block.prevID = bh.prevID;
        block.solver = bh.solver;
        block.timestamp = bh.timestamp;
        block.setCoinbase(Transaction.fromJSON(bh.coinbase));
        return block;
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

    void processBlocks() {
        List<Block> toRemove = new ArrayList<>();
        for (Block b : futureBlocks) {
            if (b.height.compareTo(ki.getChainMan().currentHeight().add(BigInteger.ONE)) == 0) {
                ki.getChainMan().addBlock(b);
                toRemove.add(b);
            }
        }
        if (!toRemove.isEmpty()) {
            futureBlocks.removeAll(toRemove);
            processBlocks();
        }
    }

}
