package com.ampex.main.blockchain;

import com.ampex.amperabase.BlockState;
import com.ampex.amperabase.IBlockAPI;
import com.ampex.amperabase.IStateManager;
import com.ampex.amperabase.ITransAPI;
import com.ampex.amperanet.packets.BlockEnd;
import com.ampex.amperanet.packets.BlockHeader;
import com.ampex.amperanet.packets.TransactionPacket;
import com.ampex.main.IKi;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class StateManager extends Thread implements IStateManager {
    private volatile ConcurrentMap<String, ConcurrentMap<BigInteger, IBlockAPI>> connBlocks = new ConcurrentHashMap<>();
    private IKi ki;
    private final Object sync = new Object();
    public StateManager(IKi ki) {
        this.ki = ki;
        addHeight = ki.getChainMan().currentHeight();
    }

    private BigInteger addHeight = BigInteger.valueOf(-1L);
    @Override
    public void addBlock(IBlockAPI block, String connID) {
        if (connBlocks.get(connID) != null) {
            connBlocks.get(connID).put(block.getHeight(), block);
        } else {
            ConcurrentMap<BigInteger, IBlockAPI> map = new ConcurrentHashMap<>();
            map.put(block.getHeight(), block);
            connBlocks.put(connID, map);
        }
        if (block.getHeight().compareTo(addHeight) > 0) {
            addHeight = block.getHeight();
        }
        //ki.debug("Adding block of height: " + block.height);
        synchronized (sync) {
            //ki.debug("Notifying State Manager");
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
                for (Map.Entry<String, ConcurrentMap<BigInteger, IBlockAPI>> connID : connBlocks.entrySet()) {
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
                        IBlockAPI b = connBlocks.get(connID.getKey()).get(ki.getChainMan().currentHeight().add(BigInteger.ONE));
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
                                    if (connBlocks.get(connID.getKey()).get(lowest).getID().equals(ki.getChainMan().getByHeight(lowest).getID())) {
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
                                            ki.debug("PREVID: " + connBlocks.get(connID.getKey()).get(h).getPrevID());
                                            ki.debug("ID: " + connBlocks.get(connID.getKey()).get(h.subtract(BigInteger.ONE)).getID());
                                            if (!connBlocks.get(connID.getKey()).get(h).getPrevID().equals(connBlocks.get(connID.getKey()).get(h.subtract(BigInteger.ONE)).getID())) {

                                                deleteMap.put(connID.getKey(), true);
                                                ki.debug("Chain is invalid, deleting blocks");
                                                continue ML;
                                            }
                                        }
                                    }
                                    ki.getChainMan().startCache(lastAgreed);
                                    ki.debug("Mitigating collision");
                                    Map<BigInteger, Set<ITransAPI>> transMap = new HashMap<>();
                                    BigInteger laCarry = new BigInteger(lastAgreed.toByteArray());
                                    Map<BigInteger, IBlockAPI> archive = new HashMap<>();
                                    BigInteger archiveHeight = new BigInteger(ki.getChainMan().currentHeight().toByteArray());
                                    for (; lastAgreed.compareTo(ki.getChainMan().currentHeight()) <= 0; lastAgreed = lastAgreed.add(BigInteger.ONE)) {
                                        archive.put(lastAgreed, ki.getChainMan().getByHeight(lastAgreed));
                                        Set<ITransAPI> transactions = new HashSet<>();
                                        for (String trans : ki.getChainMan().getByHeight(lastAgreed).getTransactionKeys()) {

                                            transactions.add(ki.getChainMan().getByHeight(lastAgreed).getTransaction(trans));
                                        }
                                        transactions.add(ki.getChainMan().getByHeight(lastAgreed).getCoinbase());
                                        transMap.put(new BigInteger(lastAgreed.toByteArray()), transactions);
                                    }

                                    ki.getChainMan().setHeight(laCarry);
                                    for (Map.Entry<BigInteger, Set<ITransAPI>> h : transMap.entrySet()) {
                                        for (ITransAPI trans : h.getValue()) {
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
                                            Set<ITransAPI> transactions = new HashSet<>();
                                            for (String key : connBlocks.get(connID.getKey()).get(laCarry).getTransactionKeys()) {
                                                transactions.add(connBlocks.get(connID.getKey()).get(laCarry).getTransaction(key));
                                            }
                                            transMap.put(laCarry, transactions);
                                        }
                                        laCarry = laCarry.add(BigInteger.ONE);
                                    }

                                    if (!doneMitigating) {

                                        for (Map.Entry<BigInteger, Set<ITransAPI>> h : transMap.entrySet()) {
                                            for (ITransAPI t : h.getValue()) {
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
                    //ki.debug("State Manager notified");
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    private void sendBlock(IBlockAPI b) {
        BlockHeader bh2 = formHeader(b);
        ki.getNetMan().broadcast(bh2);


        for (String key : b.getTransactionKeys()) {
            TransactionPacket tp = new TransactionPacket();
            tp.block = b.getID();
            tp.trans = b.getTransaction(key).serializeToAmplet().serializeToBytes();
            ki.getNetMan().broadcast(tp);
        }
        BlockEnd be = new BlockEnd();
        be.ID = b.getID();
        ki.getNetMan().broadcast(be);
    }

    private BlockHeader formHeader(IBlockAPI b) {
        BlockHeader bh = new BlockHeader();
        bh.timestamp = b.getTimestamp();
        bh.solver = b.getSolver();
        bh.prevID = b.getPrevID();
        bh.payload = b.getPayload();
        bh.merkleRoot = b.getMerkleRoot();
        bh.ID = b.getID();
        bh.height = b.getHeight();
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
