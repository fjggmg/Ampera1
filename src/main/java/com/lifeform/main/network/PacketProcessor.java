package com.lifeform.main.network;

import com.lifeform.main.IKi;
import com.lifeform.main.blockchain.Block;
import com.lifeform.main.blockchain.CPUMiner;
import com.lifeform.main.blockchain.ChainManager;
import com.lifeform.main.transactions.ITrans;
import com.lifeform.main.transactions.Input;
import com.lifeform.main.transactions.Transaction;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacketProcessor implements IPacketProcessor{

    private IKi ki;
    private PacketDispatcher pd;
    public PacketProcessor(IKi ki,IConnectionManager connMan)
    {
        this.connMan = connMan;
        this.ki = ki;
        new Thread() {
            public void run() {
                setName("PacketProcessor");
                heartbeat();
            }

        }.start();
        pd = new PacketDispatcher(ki, connMan, ki.getNetMan());

    }



    private void heartbeat()
    {
        while(run)
        {
            //ki.debug("Heartbeat of packet processor, current queue size: " + packets.size());
            if(packets.size() > 0)
            {
                if (packets.get(0) == null) {
                    packets.remove(0);
                    continue;
                }
                ki.debug("Processing packet: " + packets.get(0).toString());
                process(packets.get(0));
                packets.remove(0);
            }
            if (packets.size() == 0) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private volatile List<Object> packets = new ArrayList<>();
    private boolean run = true;
    private boolean laFlag = false;
    private boolean cuFlag = false;
    private Map<BlockHeader,List<ITrans>> bMap = new HashMap<>();
    private Map<BlockHeader,List<ITrans>> cuMap = new HashMap<>();
    private List<Block> cuBlocks = new ArrayList<>();
    private List<Block> futureBlocks = new ArrayList<>();
    private Map<String,BlockHeader> headerMap = new HashMap<>();
    private ChainManager temp;
    private IConnectionManager connMan;
    private BigInteger startHeight;
    private boolean gotPending = false;
    private boolean blocking = false;
    private boolean onRightChain = true;
    @Override
    public void process(Object packet) {

        blocking = true;
        if(packet instanceof Handshake)
        {
            ki.debug("Received handshake: ");
            Handshake hs = ((Handshake) packet);
            ki.debug("ID: " + hs.ID);
            ki.debug("Most recent block: " + hs.mostRecentBlock);
            ki.debug("version: " + hs.version);
            ki.debug("Height: " + hs.currentHeight);
            ki.debug("Chain ver: " + hs.chainVer);
            ki.debug("Address: " + connMan.getAddress());
            startHeight = hs.currentHeight;
            if (hs.chainVer != Handshake.CHAIN_VER) {
                ki.debug("Mismatched chain versions, disconnecting");
                connMan.disconnect();
                return;
            }
            if(!hs.version.equals(Handshake.VERSION)) {
                ki.debug("Mismatched network versions, disconnecting");
                connMan.disconnect();
                blocking = false;
                return;
            }
            if(hs.ID.equals(ki.getEncryptMan().getPublicKeyString()))
            {
                ki.debug("Connected to ourself, disconnecting");
                connMan.disconnect();
                blocking = false;
                return;
            }
            connMan.setID(hs.ID);
            ki.getNetMan().connectionInit(hs.ID,connMan);
            if(ki.getChainMan().currentHeight().compareTo(BigInteger.valueOf(-1L)) != 0)
            if(hs.currentHeight.compareTo(ki.getChainMan().currentHeight()) == 0 && hs.mostRecentBlock.equals(ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).ID))
            {
                for(ITrans trans:ki.getTransMan().getPending())
                {
                    TransactionPacket tp = new TransactionPacket();
                    tp.trans = trans.toJSON();
                    connMan.sendPacket(tp);
                }
            }
            if (ki.getChainMan().currentHeight().compareTo(hs.currentHeight) > 0) {
                if (!ki.getChainMan().getByHeight(hs.currentHeight).ID.equals(hs.mostRecentBlock)) {
                    onRightChain = false;
                }
            }
            if (ki.getChainMan().currentHeight().compareTo(hs.currentHeight) < 0) {
                ki.debug("Requesting blocks we're missing from the network");
                BlocksRequest br = new BlocksRequest();
                br.fromHeight = ki.getChainMan().currentHeight();
                connMan.sendPacket(br);
            } else if (ki.getChainMan().currentHeight().compareTo(BigInteger.ZERO) > 0 && !ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).ID.equals(hs.mostRecentBlock)) {
                if (ki.getChainMan().currentHeight().compareTo(hs.currentHeight) < 0) {
                    ki.debug("Discrepency between chains, starting resolution process");
                    LastAgreedStart las = new LastAgreedStart();
                    las.height = ki.getChainMan().currentHeight();
                    laFlag = true;
                    connMan.sendPacket(las);
                }

            }

        }else if(packet instanceof BlockHeader)
        {
            ki.debug("Received block header");
            BlockHeader bh = (BlockHeader) packet;
            ki.debug("Height: " + bh.height);
            headerMap.put(bh.ID,bh);
            if (laFlag && bh.laFlag)
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
            temp = new ChainManager(ki, ki.getChainMan().getChainVer(), "temp/", "tempcs.temp", "temptrans.temp", "tempextra.temp", "tempcm.temp", ki.getChainMan().getByHeight(cus.startHeight.subtract(BigInteger.ONE)), true);

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
            if(max.compareTo(ki.getChainMan().currentHeight()) <= 0){
                blocking = false;
                return;
            }
            BigInteger height = cue.startHeight;

            for(;height.compareTo(max) <= 0;height = height.add(BigInteger.ONE))
            {
                ki.debug("Verifying block with height: " + height + " before doing final commit");
                if(!temp.addBlock(heightMap.get(height))) {
                    blocking = false;
                    return;
                }
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
                    blocking = false;
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
            bh.laFlag = true;
            connMan.sendPacket(bh);
        }else if(packet instanceof LastAgreedStart)
        {
            ki.debug("Received last agreed start");
            LastAgreedStart las = (LastAgreedStart) packet;
            if (onRightChain) {
                ResetRequest rr = new ResetRequest();
                BlockHeader bh = formHeader(ki.getChainMan().getByHeight(las.height));
                rr.proof = bh;
                connMan.sendPacket(rr);
                return;
            }

            BlockHeader bh;
            Block b = ki.getChainMan().getByHeight(las.height);
            bh = formHeader(b);
            bh.laFlag = true;
            connMan.sendPacket(bh);
        }else if(packet instanceof TransactionPacket)
        {
            ki.debug("Received transaction packet");
            TransactionPacket tp = (TransactionPacket) packet;
            if(tp.block == null || tp.block.isEmpty())
            {
                if(ki.getTransMan().verifyTransaction(Transaction.fromJSON(tp.trans))){
                    ITrans trans = Transaction.fromJSON(tp.trans);
                    if(trans == null || trans.getInputs() == null){
                        blocking = false;
                        return;
                    }
                    for(ITrans t:ki.getTransMan().getPending())
                    {
                        for(Input i:t.getInputs())
                        {
                            for(Input i2:trans.getInputs())
                            {
                                if(i.getID().equals(i2.getID())){
                                    ki.debug("Got bad transaction from network, double spend.");
                                    blocking = false;
                                    return;
                                }
                            }
                        }
                    }
                    ki.getTransMan().getPending().add(trans);
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
                        ki.debug("Adding transaction to block list");
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
                if (block == null) {
                    ki.debug("Something fucked up, the block we received is null because of something");
                    return;
                }
                for (ITrans t : trans) {
                    block.addTransaction(t);
                }
                cuBlocks.add(block);
            }else {

                BlockEnd be = (BlockEnd) packet;
                BlockHeader bh = headerMap.get(be.ID);
                List<ITrans> trans = bMap.get(bh);
                Block block = formBlock(bh);
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
                if(block.height.compareTo(ki.getChainMan().currentHeight().add(BigInteger.ONE)) == 0)
                {
                    ki.debug("Verifying block");
                    if(!ki.getChainMan().addBlock(block))
                    {
                        onRightChain = false;
                        if(ki.getNetMan().isRelay())
                        {

                            BadBlockEnd bbe = new BadBlockEnd();
                            bbe.ID = be.ID;
                            ki.getNetMan().broadcast(bbe);
                        }
                     LastAgreedStart las = new LastAgreedStart();
                     las.height = ki.getChainMan().currentHeight();
                     laFlag = true;
                     connMan.sendPacket(las);
                    }else {
                        ki.debug("Block verified and added");
                        if(ki.getNetMan().isRelay())
                        {
                            ki.debug("Relaying block now");
                            ki.getNetMan().broadcast(packet);
                        }
                        onRightChain = true;
                        processBlocks();
                        if(ki.getMinerMan().isMining())
                        {
                            ki.debug("Restarting miners");
                            CPUMiner.height = ki.getChainMan().currentHeight().add(BigInteger.ONE);
                            CPUMiner.prevID = ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).ID;

                            ki.getMinerMan().restartMiners(ki.getMinerMan().getPreviousCount());
                        }
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

        }else if(packet instanceof PendingTransactionRequest)
        {
            for(ITrans trans:ki.getTransMan().getPending())
            {
                TransactionPacket tp = new TransactionPacket();
                tp.trans = trans.toJSON();
                connMan.sendPacket(tp);
            }
        }else if(packet instanceof BadBlockEnd)
        {
            BadBlockEnd bbe = (BadBlockEnd) packet;
            bMap.remove(headerMap.get(bbe.ID));
            headerMap.remove(bbe.ID);
            onRightChain = false;
        } else if (packet instanceof ResetRequest) {
            ki.debug("Received a reset request");
            ResetRequest rr = (ResetRequest) packet;
            if (rr.proof.height.compareTo(ki.getChainMan().currentHeight()) == 0 && laFlag) {
                if (rr.proof.ID.equals(ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).ID)) {
                    //this should be sufficient check but really we need to do a full fucking check of the block, will implement ease of use method for this later
                    if (rr.proof.prevID.equals(ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).prevID)) {
                        ki.debug("Reset request is legitimate, reseting block chain and transactions. This may take some time");
                        List<Block> blocks = new ArrayList<>();
                        BigInteger height = BigInteger.ZERO;
                        for (; height.compareTo(ki.getChainMan().currentHeight()) <= 0; height = height.add(BigInteger.ONE)) {
                            blocks.add(ki.getChainMan().getByHeight(height));
                        }
                        ki.getChainMan().clearFile();
                        ki.getTransMan().clear();
                        for (Block b : blocks) {
                            if (!ki.getChainMan().addBlock(b)) {
                                ki.debug("The block chain is corrupted beyond repair, you will need to manually delete the chain and transaction folders AFTER closing the program. After restarting the program will redownload the chain and should work correctly");
                                return;
                            }
                        }
                    }
                }
            }
        }
        blocking = false;
    }

    @Override
    public void enqueue(Object packet) {

        ki.debug("Enqueued new packet for processing: " + packet.toString());
        packets.add(packet);
    }

    private void sendFromHeight(BigInteger height)
    {
        for(;height.compareTo(ki.getChainMan().currentHeight()) <= 0;height = height.add(BigInteger.ONE))
        {
            if(height.compareTo(BigInteger.valueOf(-1L)) != 0)
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
        if(b.height.compareTo(ki.getChainMan().currentHeight()) == 0)
        {
            for(ITrans trans:ki.getTransMan().getPending())
            {
                TransactionPacket tp = new TransactionPacket();
                tp.trans = trans.toJSON();
                connMan.sendPacket(tp);
            }
        }
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
    private BlockHeader formHeader(Block b)
    {
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


