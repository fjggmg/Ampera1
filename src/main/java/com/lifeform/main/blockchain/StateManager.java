package com.lifeform.main.blockchain;

import com.lifeform.main.IKi;
import com.lifeform.main.network.packets.BlockEnd;
import com.lifeform.main.network.packets.BlockHeader;
import com.lifeform.main.network.packets.TransactionPacket;
import com.lifeform.main.transactions.ITrans;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class StateManager extends Thread implements IStateManager {
    private volatile ConcurrentMap<String, ConcurrentMap<BigInteger, Block>> connBlocks = new ConcurrentHashMap<>();
    private IKi ki;
    private final Object sync = new Object();
    public StateManager(IKi ki) {
        this.ki = ki;
        addHeight = ki.getChainMan().currentHeight();
    }

    private BigInteger addHeight = BigInteger.valueOf(-1L);
    @Override
    public void addBlock(Block block, String connID) {
        if (connBlocks.get(connID) != null) {
            connBlocks.get(connID).put(block.height, block);
        } else {
            ConcurrentMap<BigInteger, Block> map = new ConcurrentHashMap<>();
            map.put(block.height, block);
            connBlocks.put(connID, map);
        }
        if (block.height.compareTo(addHeight) > 0) {
            addHeight = block.height;
        }
        ki.debug("Adding block of height: " + block.height);
        synchronized (sync) {
            ki.debug("Notifying State Manager");
            sync.notifyAll();
        }

    }

    private volatile List<String> deleted = new ArrayList<>();
    private volatile Map<String, Boolean> deleteMap = new HashMap<>();
    private volatile Map<String, Boolean> sentLA = new HashMap<>();
    public void run() {
        setName("StateManager");
        ML:
        while (true) {
            for (Map.Entry<String, Boolean> key : deleteMap.entrySet()) {
                if (key.getValue()) {
                    connBlocks.remove(key.getKey());
                    deleted.add(key.getKey());
                }
            }
            for (String d : deleted) {
                deleteMap.remove(d);
            }
            deleted.clear();

            if (addHeight.compareTo(ki.getChainMan().currentHeight()) > 0) {

                //ki.debug("State changed, adjusting block chain.");
                for (Map.Entry<String, ConcurrentMap<BigInteger, Block>> connID : connBlocks.entrySet()) {
                    //TODO works with linear progression, will cause small leak with mitigation
                    connID.getValue().remove(ki.getChainMan().currentHeight().subtract(BigInteger.valueOf(100L)));
                    if (ki.getNetMan().getConnection(connID.getKey()) == null) {
                        ki.debug("Connection: " + connID + " has gone null, removing from list");
                        deleteMap.put(connID.getKey(), true);
                        continue;
                    }
                    if (!ki.getNetMan().getConnection(connID.getKey()).isConnected()) {
                        ki.debug("Connection: " + connID + " has disconnected, removing from list");
                        deleteMap.put(connID.getKey(), true);
                        continue;
                    }

                    if (connBlocks.get(connID.getKey()).get(ki.getChainMan().currentHeight().add(BigInteger.ONE)) != null) {
                        Block b = connBlocks.get(connID.getKey()).get(ki.getChainMan().currentHeight().add(BigInteger.ONE));
                        BlockState bs = ki.getChainMan().addBlock(b);
                        if (!bs.success()) {
                            if (bs.retry()) {
                                for (int i = 0; i < 5; i++) {
                                    if (ki.getChainMan().addBlock(b).success()) {
                                        if (ki.getNetMan().isRelay()) {
                                            ki.debug("Block verified, broadcasting.");
                                            sendBlock(b);
                                        }
                                        sentLA.put(connID.getKey(), false);
                                        continue ML;
                                    }
                                }
                            }
                        } else {

                            if (ki.getNetMan().isRelay()) {
                                ki.debug("Block verified, broadcasting.");
                                sendBlock(b);
                            }
                            sentLA.put(connID.getKey(), false);
                            continue ML;
                        }

                        if (bs != BlockState.PREVID_MISMATCH) {
                            addHeight = ki.getChainMan().currentHeight();
                            deleteMap.put(connID.getKey(), true);
                            continue ML;
                        }
                    }

                    for (BigInteger height : connBlocks.get(connID.getKey()).keySet()) {
                        if (height.compareTo(ki.getChainMan().currentHeight()) > 0) {
                            if (height.compareTo(ki.getChainMan().currentHeight().add(BigInteger.ONE)) == 0) {

                                BigInteger lowest = BigInteger.ZERO;
                                for (BigInteger h : connBlocks.get(connID.getKey()).keySet()) {
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
                                    if (connBlocks.get(connID.getKey()).get(lowest) == null) {
                                        break;
                                    }
                                    if (connBlocks.get(connID.getKey()).get(lowest).ID.equals(ki.getChainMan().getByHeight(lowest).ID)) {
                                        lastAgreed = lowest;
                                        lowest = lowest.add(BigInteger.ONE);
                                    } else {
                                        foundLastAgreed = true;
                                    }

                                }


                                if (!foundLastAgreed) {
                                    //TODO removing this until we start working on mitigation. FIX BY 0.19
                                    /*
                                    ki.debug("Failed to find last agreed block");
                                    if ((sentLA.get(connID.getKey()) != null && !sentLA.get(connID.getKey())) || sentLA.get(connID.getKey()) == null) {
                                        sentLA.put(connID.getKey(), true);
                                        LastAgreedStart las = new LastAgreedStart();
                                        las.height = ki.getChainMan().currentHeight();
                                        ki.getNetMan().getConnection(connID.getKey()).sendPacket(las);
                                    }
                                    */
                                } else {
                                    ki.debug("Found last agreed block");
                                    if(lastAgreed.compareTo(BigInteger.ZERO) == 0)
                                    {
                                        ki.debug("Last agreed is 0, discarding");
                                        deleteMap.put(connID.getKey(), true);
                                        continue ML;
                                    }
                                    for (BigInteger h : connBlocks.get(connID.getKey()).keySet()) {

                                        if (connBlocks.get(connID.getKey()).get(h.subtract(BigInteger.ONE)) != null) {
                                            ki.debug("PREVID: " + connBlocks.get(connID.getKey()).get(h).prevID);
                                            ki.debug("ID: " + connBlocks.get(connID.getKey()).get(h.subtract(BigInteger.ONE)).ID);
                                            if (!connBlocks.get(connID.getKey()).get(h).prevID.equals(connBlocks.get(connID.getKey()).get(h.subtract(BigInteger.ONE)).ID)) {

                                                deleteMap.put(connID.getKey(), true);
                                                ki.debug("Chain is invalid, deleting blocks");
                                                continue ML;
                                            }
                                        }
                                    }
                                    ki.getChainMan().startCache(lastAgreed);
                                    ki.debug("Mitigating collision");
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
                                        transactions.add(ki.getChainMan().getByHeight(lastAgreed).getCoinbase());
                                        transMap.put(new BigInteger(lastAgreed.toByteArray()), transactions);
                                    }

                                    ki.getChainMan().setHeight(laCarry);
                                    for (Map.Entry<BigInteger, Set<ITrans>> h : transMap.entrySet()) {
                                        for (ITrans trans : h.getValue()) {
                                            ki.getTransMan().undoTransaction(trans);
                                        }
                                    }

                                    BigInteger laCarry2 = new BigInteger(laCarry.toByteArray());
                                    boolean doneMitigating = false;
                                    transMap.clear();
                                    ki.debug("Undid chain and transactions back to last agreed block");
                                    while (!doneMitigating) {
                                        if (connBlocks.get(connID.getKey()).get(laCarry) == null) {
                                            doneMitigating = true;

                                        } else if (!ki.getChainMan().addBlock(connBlocks.get(connID.getKey()).get(laCarry)).success()) {
                                            break;
                                        } else {
                                            Set<ITrans> transactions = new HashSet<>();
                                            for (String key : connBlocks.get(connID.getKey()).get(laCarry).getTransactionKeys()) {
                                                transactions.add(connBlocks.get(connID.getKey()).get(laCarry).getTransaction(key));
                                            }
                                            transMap.put(laCarry, transactions);
                                        }
                                        laCarry = laCarry.add(BigInteger.ONE);
                                    }

                                    if (!doneMitigating) {

                                        for (Map.Entry<BigInteger, Set<ITrans>> h : transMap.entrySet()) {
                                            for (ITrans t : h.getValue()) {
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

                                    } else {
                                        ki.debug("Collision mitigated");
                                        sentLA.put(connID.getKey(), false);
                                        sendFromHeight(laCarry2);
                                    }
                                    ki.getChainMan().stopCache();

                                }


                            } else {
                                /*

                                //request blocks here
                                BlockRequest br = new BlockRequest();
                                br.fromHeight = ki.getChainMan().currentHeight();
                                br.lite = ki.getOptions().lite;
                                ki.getNetMan().getConnection(connID).sendPacket(br);
                                */
                            }
                        }
                    }
                }
            }

            synchronized (sync) {
                try {
                    if (addHeight.compareTo(ki.getChainMan().currentHeight()) <= 0)
                        sync.wait();
                    ki.debug("State Manager notified");
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    private void sendBlock(Block b) {
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
        bh.coinbase = b.getCoinbase().serializeToAmplet().serializeToBytes();
        return bh;
    }

    private void sendFromHeight(BigInteger height) {
        for (; height.compareTo(ki.getChainMan().currentHeight()) <= 0; height = height.add(BigInteger.ONE)) {
            if (height.compareTo(BigInteger.valueOf(-1L)) != 0)
                sendBlock(ki.getChainMan().getByHeight(height));
        }

    }


}
