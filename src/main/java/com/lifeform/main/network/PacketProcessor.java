package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.blockchain.ChainManager;
import com.lifeform.main.transactions.ITrans;
import com.lifeform.main.transactions.Transaction;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacketProcessor implements IPacketProcessor{

    private IKi ki;
    public PacketProcessor(IKi ki,IConnectionManager connMan)
    {
        this.connMan = connMan;
        this.ki = ki;
    }
    private boolean laFlag = false;
    private boolean cuFlag = false;
    private Map<BlockHeader,List<ITrans>> bMap = new HashMap<>();
    private Map<BlockHeader,List<ITrans>> cuMap = new HashMap<>();
    private List<Block> cuBlocks = new ArrayList<>();
    private List<Block> futureBlocks = new ArrayList<>();
    private Map<String,BlockHeader> headerMap = new HashMap<>();
    private ChainManager temp;
    private IConnectionManager connMan;
    @Override
    public void process(Object packet) {
        if(packet instanceof Handshake)
        {
            ki.debug("Received handshake: ");
            Handshake hs = ((Handshake) packet);
            ki.debug("ID: " + hs.ID);
            ki.debug("Most recent block: " + hs.mostRecentBlock);
            ki.debug("version: " + hs.version);
            ki.debug("Height: " + hs.currentHeight);

            if(!hs.version.equals(Handshake.VERSION)) {
                ki.debug("Mismatched network versions, disconnecting");
                connMan.disconnect();
                return;
            }
            if(hs.ID.equals(ki.getEncryptMan().getPublicKeyString()))
            {
                ki.debug("Connected to ourself, disconnecting");
                connMan.disconnect();
                return;
            }
            connMan.setID(hs.ID);
            ki.getNetMan().connectionInit(hs.ID,connMan);
            if(ki.getChainMan().currentHeight().compareTo(BigInteger.ZERO) > 0 && !ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).ID.equals(hs.mostRecentBlock))
            {
                if(ki.getChainMan().currentHeight().compareTo(hs.currentHeight) < 0)
                {
                    ki.debug("Discrepency between chains, starting resolution process");
                    LastAgreedStart las = new LastAgreedStart();
                    las.height = ki.getChainMan().currentHeight();
                    laFlag = true;
                    connMan.sendPacket(las);
                }

            }else if(ki.getChainMan().currentHeight().compareTo(hs.currentHeight) < 0)
            {
                ki.debug("Requesting blocks we're missing from the network");
                BlocksRequest br = new BlocksRequest();
                br.fromHeight = ki.getChainMan().currentHeight();
                connMan.sendPacket(br);
            }
        }else if(packet instanceof BlockHeader)
        {
            ki.debug("Received block header");
            BlockHeader bh = (BlockHeader) packet;
            ki.debug("Height: " + bh.height);
            headerMap.put(bh.ID,bh);
            if(laFlag)
            {
                if(ki.getChainMan().getByHeight(bh.height).ID.equals(bh.ID))
                {
                    LastAgreedEnd lae = new LastAgreedEnd();
                    lae.height = bh.height;
                    laFlag = false;
                    connMan.sendPacket(lae);
                }else{
                    LastAgreedContinue lac = new LastAgreedContinue();
                    lac.height = bh.height.subtract(BigInteger.ONE);
                    connMan.sendPacket(lac);
                }
            }else if(cuFlag){
                cuMap.put(bh,new ArrayList<>());
            }else{
                bMap.put(bh,new ArrayList<>());
                if(ki.getNetMan().isRelay())
                {
                    ki.getNetMan().broadcast(packet);
                }
            }
        }else if(packet instanceof LastAgreedEnd)
        {
            ki.debug("Received last agreed end");
            LastAgreedEnd lae = (LastAgreedEnd) packet;
            ChainUpStart cus = new ChainUpStart();
            cus.startHeight = lae.height = lae.height.add(BigInteger.ONE);
            connMan.sendPacket(cus);
            sendFromHeight(cus.startHeight);
            ChainUpEnd cue = new ChainUpEnd();
            cue.startHeight = cus.startHeight;
            connMan.sendPacket(cue);
        }else if(packet instanceof ChainUpStart)
        {
            cuFlag = true;
            ChainUpStart cus = (ChainUpStart) packet;
            temp = new ChainManager(ki,ChainManager.POW_CHAIN,"temp/","tempcs.temp","temptrans.temp","tempextra.temp","tempcm.temp",ki.getChainMan().getByHeight(cus.startHeight.subtract(BigInteger.ONE)));

        }else if(packet instanceof ChainUpEnd)
        {
            ki.debug("Received chain update end");
            ChainUpEnd cue = (ChainUpEnd) packet;
            cuFlag = false;
            Map<BigInteger,Block> heightMap = new HashMap<>();
            BigInteger max = BigInteger.ZERO;
            for(Block b:cuBlocks)
            {
                if(b.height.compareTo(max) > 0) max = b.height;
                heightMap.put(b.height,b);
            }
            ki.debug("Checking to make sure is forward moving update");
            if(max.compareTo(ki.getChainMan().currentHeight()) <= 0) return;
            BigInteger height = cue.startHeight;

            for(;height.compareTo(max) <= 0;height = height.add(BigInteger.ONE))
            {
                ki.debug("Verifying block with height: " + height + " before doing final commit");
                if(!temp.addBlock(heightMap.get(height))) return;
            }
            ki.debug("Undoing chain to height: " + cue.startHeight);
            if(ki.getChainMan().currentHeight().compareTo(cue.startHeight) >= 0)
            ki.getChainMan().undoToBlock(ki.getChainMan().getByHeight(cue.startHeight).ID);
            height = cue.startHeight;
            for(;height.compareTo(max) <= 0;height = height.add(BigInteger.ONE))
            {
                ki.debug("Adding block with height: " + height + " to local files");
                if(!ki.getChainMan().addBlock(heightMap.get(height))){
                    ki.getMainLog().info("Error updating chain to larger competing chain, chain unfinished, will attempt to pull updates for this chain");
                    return;
                }
            }

        }else if(packet instanceof LastAgreedContinue)
        {
            ki.debug("received last agreed continue");
            LastAgreedContinue lac = (LastAgreedContinue) packet;
            BlockHeader bh;
            Block b = ki.getChainMan().getByHeight(lac.height);
            bh = formHeader(b);
            connMan.sendPacket(bh);
        }else if(packet instanceof LastAgreedStart)
        {
            ki.debug("Received last agreed start");
            LastAgreedStart las = (LastAgreedStart) packet;
            BlockHeader bh;
            Block b = ki.getChainMan().getByHeight(las.height);
            bh = formHeader(b);
            connMan.sendPacket(bh);
        }else if(packet instanceof TransactionPacket)
        {
            ki.debug("Received transaction packet");
            TransactionPacket tp = (TransactionPacket) packet;
            if(tp.block == null || tp.block.isEmpty())
            {
                if(ki.getTransMan().verifyTransaction(Transaction.fromJSON(tp.trans))){
                    ki.getTransMan().getPending().add(Transaction.fromJSON(tp.trans));
                    if(ki.getNetMan().isRelay())
                    {
                        ki.getNetMan().broadcastAllBut(connMan.getID(),packet);
                    }
                }
            }else{
                if(cuFlag)
                {
                    if(cuMap.get(headerMap.get(tp.block)) != null)
                    {
                        cuMap.get(headerMap.get(tp.block)).add(Transaction.fromJSON(tp.trans));
                    }
                }else{
                    if(bMap.get(headerMap.get(tp.block)) != null)
                    {
                        bMap.get(headerMap.get(tp.block)).add(Transaction.fromJSON(tp.trans));
                        if(ki.getNetMan().isRelay())
                        {
                            ki.getNetMan().broadcast(packet);
                        }
                    }
                }
            }
        }else if(packet instanceof BlockEnd)
        {
            ki.debug("Received block end");
            if(cuFlag)
            {
                BlockEnd be = (BlockEnd) packet;
                BlockHeader bh = headerMap.get(be.ID);
                List<ITrans> trans = cuMap.get(bh);
                Block block = formBlock(bh);
                for (ITrans t : trans) {
                    block.addTransaction(t);
                }
                cuBlocks.add(block);
            }else {
                if(ki.getNetMan().isRelay())
                {
                    ki.getNetMan().broadcast(packet);
                }
                BlockEnd be = (BlockEnd) packet;
                BlockHeader bh = headerMap.get(be.ID);
                List<ITrans> trans = bMap.get(bh);
                Block block = formBlock(bh);
                for (ITrans t : trans) {
                    block.addTransaction(t);
                }
                if(block.height.compareTo(ki.getChainMan().currentHeight().add(BigInteger.ONE)) == 0)
                {
                    if(!ki.getChainMan().addBlock(block))
                    {
                     LastAgreedStart las = new LastAgreedStart();
                     las.height = ki.getChainMan().currentHeight();
                     laFlag = true;
                     connMan.sendPacket(las);
                    }else {
                        processBlocks();
                    }
                }else if(block.height.compareTo(ki.getChainMan().currentHeight().add(BigInteger.ONE)) > 0){
                    /*
                    BlocksRequest br = new BlocksRequest();
                    br.fromHeight = ki.getChainMan().currentHeight().add(BigInteger.ONE);
                    connMan.sendPacket(br);
                     */
                    futureBlocks.add(block);
                }
                /*else{
                    if(ki.getChainMan().getByHeight(block.height).ID.equals(block.ID))
                    {
                        BigInteger height = block.height;
                        sendFromHeight(height);
                    }
                }*/
            }

        }else if(packet instanceof BlocksRequest)
        {
            ki.debug("Received blocks request");
            BlocksRequest br = (BlocksRequest) packet;
            if(br.fromHeight.compareTo(ki.getChainMan().currentHeight()) <= 0)
            sendFromHeight(br.fromHeight);
            else{
                LastAgreedStart las = new LastAgreedStart();
                las.height = ki.getChainMan().currentHeight();
                laFlag = true;
                connMan.sendPacket(las);
            }

        }
    }

    private void sendFromHeight(BigInteger height)
    {
        for(;height.compareTo(ki.getChainMan().currentHeight()) <= 0;height = height.add(BigInteger.ONE))
        {
            sendBlock(height);
        }
    }

    private void sendBlock(BigInteger height)
    {
        Block b = ki.getChainMan().getByHeight(height);
        BlockHeader bh2 = formHeader(b);
        connMan.sendPacket(bh2);


        for(String key:b.getTransactionKeys())
        {
            TransactionPacket tp = new TransactionPacket();
            tp.block = b.ID;
            tp.trans = b.getTransaction(key).toJSON();
            connMan.sendPacket(tp);
        }
        BlockEnd be = new BlockEnd();
        be.ID = b.ID;
        connMan.sendPacket(be);
    }

    private void processBlocks()
    {
        List<Block> toRemove = new ArrayList<>();
        for(Block b:futureBlocks)
        {
            if(b.height.compareTo(ki.getChainMan().currentHeight().add(BigInteger.ONE)) == 0)
            {
                ki.getChainMan().addBlock(b);
                toRemove.add(b);
            }
        }
        if(!toRemove.isEmpty())
        {
            futureBlocks.removeAll(toRemove);
            processBlocks();
        }
    }
    private Block formBlock(BlockHeader bh)
    {
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
    private BlockHeader formHeader(Block b)
    {
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


