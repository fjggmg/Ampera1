package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.blockchain.CPUMiner;
import com.lifeform.main.transactions.ITrans;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

public class BlockEnd implements Serializable, Packet {
    public String ID;

    @Override
    public void process(IKi ki, IConnectionManager connMan, PacketGlobal pg) {
        ki.debug("Received block end");
        if (pg.cuFlag) {
            BlockHeader bh = pg.headerMap.get(ID);
            List<ITrans> trans = pg.cuMap.get(bh);
            Block block = pg.formBlock(bh);
            if (block == null) {
                ki.debug("Something fucked up, the block we received is null because of something");
                return;
            }
            for (ITrans t : trans) {
                block.addTransaction(t);
            }
            pg.cuBlocks.add(block);
            BlockAck ba = new BlockAck();
            ba.height = block.height;
            ba.verified = false;
            connMan.sendPacket(ba);
        } else {

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
            if (block.height.compareTo(ki.getChainMan().currentHeight().add(BigInteger.ONE)) == 0) {
                ki.debug("Verifying block");
                if (!ki.getChainMan().addBlock(block)) {
                    pg.onRightChain = false;
                    if (ki.getNetMan().isRelay()) {

                        BadBlockEnd bbe = new BadBlockEnd();
                        bbe.ID = ID;
                        ki.getNetMan().broadcast(bbe);
                    }
                    BlockAck ba = new BlockAck();
                    ba.height = block.height;
                    ba.verified = false;
                    connMan.sendPacket(ba);
                    LastAgreedStart las = new LastAgreedStart();
                    las.height = ki.getChainMan().currentHeight();
                    pg.laFlag = true;
                    connMan.sendPacket(las);
                } else {
                    ki.debug("Block verified and added");
                    if (ki.getNetMan().isRelay()) {
                        ki.debug("Relaying block now");
                        ki.getNetMan().broadcast(this);
                    }
                    pg.onRightChain = true;
                    pg.processBlocks();
                    if (ki.getMinerMan().isMining()) {
                        ki.debug("Restarting miners");
                        /** old miner stuff
                        CPUMiner.height = ki.getChainMan().currentHeight().add(BigInteger.ONE);
                        CPUMiner.prevID = ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).ID;
                        */
                        ki.getMinerMan().restartMiners();
                    }
                    BlockAck ba = new BlockAck();
                    ba.height = block.height;
                    ba.verified = true;
                    connMan.sendPacket(ba);
                }
            } else if (block.height.compareTo(ki.getChainMan().currentHeight().add(BigInteger.ONE)) > 0) {
                    /*
                    BlockRequest br = new BlockRequest();
                    br.fromHeight = ki.getChainMan().currentHeight().add(BigInteger.ONE);
                    connMan.sendPacket(br);
                     */
                pg.futureBlocks.add(block);
            }
            BlockAck ba = new BlockAck();
            ba.height = block.height;
            ba.verified = false;
            connMan.sendPacket(ba);
                /*else{
                    if(ki.getChainMan().getByHeight(block.height).ID.equals(block.ID))
                    {
                        BigInteger height = block.height;
                        sendFromHeight(height);
                    }
                }*/

        }
    }

    @Override
    public int packetType() {
        return PacketType.BE.getIndex();
    }
}
