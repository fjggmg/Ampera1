package com.lifeform.main.blockchain;

import com.lifeform.main.IKi;
import com.lifeform.main.network.BlockEnd;
import com.lifeform.main.network.BlockHeader;
import com.lifeform.main.network.LastAgreedStart;
import com.lifeform.main.network.TransactionPacket;
import com.lifeform.main.transactions.ITrans;

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

    private volatile List<String> deleted = new ArrayList<>();
    private volatile Map<String, Boolean> deleteMap = new HashMap<>();
    public void run() {

        ML:
        while (true) {
            for (String key : deleteMap.keySet()) {
                if (deleteMap.get(key)) {
                    connBlocks.remove(key);
                    deleted.add(key);
                }
            }
            for (String d : deleted) {
                deleteMap.remove(d);
            }
            deleted.clear();

            if (changed) {
                for (String connID : connBlocks.keySet()) {

                    if (connBlocks.get(connID).get(ki.getChainMan().currentHeight().add(BigInteger.ONE)) != null) {
                        Block b = connBlocks.get(connID).get(ki.getChainMan().currentHeight().add(BigInteger.ONE));
                        BlockState bs = ki.getChainMan().addBlock(b);
                        if (!bs.success()) {
                            if (bs.retry()) {
                                for (int i = 0; i < 5; i++) {
                                    if (ki.getChainMan().addBlock(b).success()) {
                                        continue ML;
                                    }
                                }
                            }
                        } else {
                            continue ML;
                        }
                    }
                    for (BigInteger height : connBlocks.get(connID).keySet()) {
                        if (height.compareTo(ki.getChainMan().currentHeight()) > 0) {
                            if (height.compareTo(ki.getChainMan().currentHeight().add(BigInteger.ONE)) == 0) {

                                BigInteger lowest = BigInteger.ZERO;
                                for (BigInteger h : connBlocks.get(connID).keySet()) {
                                    if (h.compareTo(lowest) < 0 || lowest.compareTo(BigInteger.ZERO) == 0) {
                                        lowest = h;
                                    }
                                }
                                boolean foundLastAgreed = false;
                                BigInteger lastAgreed = BigInteger.ZERO;
                                while (!foundLastAgreed) {
                                    if (lowest.compareTo(ki.getChainMan().currentHeight()) >= 0) {
                                        break;
                                    }
                                    if (connBlocks.get(connID).get(lowest) == null) {
                                        break;
                                    }
                                    if (connBlocks.get(connID).get(lowest).ID.equals(ki.getChainMan().getByHeight(lowest).ID)) {
                                        lastAgreed = lowest;
                                        lowest = lowest.add(BigInteger.ONE);
                                    } else {
                                        foundLastAgreed = true;
                                    }

                                }

                                if (!foundLastAgreed) {
                                    LastAgreedStart las = new LastAgreedStart();
                                    las.height = ki.getChainMan().currentHeight();
                                    ki.getNetMan().getConnection(connID).sendPacket(las);
                                } else {
                                    for (BigInteger h : connBlocks.get(connID).keySet()) {
                                        if (connBlocks.get(connID).get(h.subtract(BigInteger.ONE)) != null)
                                            if (!connBlocks.get(connID).get(h).prevID.matches(connBlocks.get(connID).get(h.subtract(BigInteger.ONE)).ID)) {
                                                deleteMap.put(connID, true);
                                                continue ML;
                                            }
                                    }
                                    Map<BigInteger, Set<ITrans>> transMap = new HashMap<>();
                                    BigInteger laCarry = new BigInteger(lastAgreed.toByteArray());
                                    Map<BigInteger, Block> archive = new HashMap<>();
                                    BigInteger archiveHeight = new BigInteger(ki.getChainMan().currentHeight().toByteArray());
                                    for (; lastAgreed.compareTo(ki.getChainMan().currentHeight()) <= 0; lastAgreed = lastAgreed.add(BigInteger.ONE)) {
                                        archive.put(lastAgreed, ki.getChainMan().getByHeight(lastAgreed));
                                        Set<ITrans> transactions = new HashSet<>();
                                        for (String trans : ki.getChainMan().getByHeight(lastAgreed).getTransactionKeys()) {

                                            transactions.add(ki.getChainMan().getByHeight(lastAgreed).getTransaction(trans));
                                        }
                                        transMap.put(new BigInteger(lastAgreed.toByteArray()), transactions);
                                    }

                                    ki.getChainMan().setHeight(laCarry);
                                    for (BigInteger h : transMap.keySet()) {
                                        for (ITrans trans : transMap.get(h)) {
                                            ki.getTransMan().undoTransaction(trans);
                                        }
                                    }

                                    BigInteger laCarry2 = new BigInteger(laCarry.toByteArray());
                                    boolean doneMitigating = false;
                                    transMap.clear();
                                    while (!doneMitigating) {
                                        if (connBlocks.get(connID).get(laCarry) == null) {
                                            doneMitigating = true;

                                        } else if (!ki.getChainMan().addBlock(connBlocks.get(connID).get(laCarry)).success()) {
                                            break;
                                        } else {
                                            Set<ITrans> transactions = new HashSet<>();
                                            for (String key : connBlocks.get(connID).get(laCarry).getTransactionKeys()) {
                                                transactions.add(connBlocks.get(connID).get(laCarry).getTransaction(key));
                                            }
                                            transMap.put(laCarry, transactions);
                                        }
                                        laCarry = laCarry.add(BigInteger.ONE);
                                    }

                                    if (!doneMitigating) {
                                        for (BigInteger h : transMap.keySet()) {
                                            for (ITrans t : transMap.get(h)) {
                                                ki.getTransMan().undoTransaction(t);
                                            }
                                        }

                                        ki.getChainMan().setHeight(laCarry2);
                                        for (; laCarry2.compareTo(archiveHeight) == 0; laCarry2 = laCarry2.add(BigInteger.ONE)) {
                                            if (!ki.getChainMan().addBlock(archive.get(laCarry2)).success()) {
                                                ki.debug("During a collision mitigation, we archived our blocks to be able to revert to. They have become corrupted in memory and we will not be able to mitigate this collision, the chain should rebuild itself automatically.");
                                                break;
                                            }
                                        }
                                    }


                                }


                            }
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
