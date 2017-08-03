package com.lifeform.main.blockchain;

import com.lifeform.main.IKi;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.JSONManager;
import com.lifeform.main.data.Utils;
import com.lifeform.main.data.files.StringFileHandler;
import com.lifeform.main.transactions.MKiTransaction;
import org.json.simple.JSONObject;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Bryan on 5/28/2017.
 *
 * Block time = 5m
 *
 */
public class ChainManager implements IChainMan {
    private IKi ki;
    private boolean canMine = true;
    Block current;
    BigInteger currentDifficulty = new BigInteger("00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",16);
    BigInteger currentHeight = BigInteger.valueOf(-1L);
    DB csDB;
    DB tmDB;
    DB exDB;
    DB cmDB;
    private boolean lock = false;
    private Map<BigInteger,Block> verifyLater = new HashMap<>();
    /**
     * stupid way to store the chain for easy access momentarily
     */
    Map<String, Block> blockchainMap = new HashMap<>();
    Map<BigInteger,Block> heightMap = new HashMap<>();
    ConcurrentMap<String,String> csMap;
    ConcurrentMap<String,String> tmMap;
    ConcurrentMap<String,String> exMap;
    ConcurrentMap<String,String> cmMap;
    private String fileName = "block.data";
    private String folderName = "blocks/";
    /**
     * current implementation is for multi-chain PoW and PoS system with
     * different chain IDs listed as finals at bottom of file
     * @param chainID
     */
    public ChainManager(IKi ki, short chainID)
    {
        this.ki = ki;

        csDB = DBMaker.fileDB("chain.state").fileMmapEnableIfSupported().transactionEnable().make();

        csMap = csDB.hashMap("csDB", Serializer.STRING,Serializer.STRING).createOrOpen();

        tmDB = DBMaker.fileDB("transaction.meta").fileMmapEnableIfSupported().transactionEnable().make();

        tmMap = tmDB.hashMap("tmDB",Serializer.STRING,Serializer.STRING).createOrOpen();

        exDB = DBMaker.fileDB("extra.chains").fileMmapEnableIfSupported().transactionEnable().make();

        exMap = exDB.hashMap("exDB",Serializer.STRING,Serializer.STRING).createOrOpen();

        cmDB = DBMaker.fileDB("chain.meta").fileMmapEnableIfSupported().transactionEnable().make();

        cmMap = cmDB.hashMap("cmDB", Serializer.STRING,Serializer.STRING).createOrOpen();

    }

    public void close()
    {
        csDB.commit();
        csDB.close();
        tmDB.commit();
        tmDB.close();
        exDB.commit();
        exDB.close();
        cmDB.commit();
        cmDB.close();
    }

    public synchronized boolean addBlock(Block block){
       
        if(!verifyBlock(block)) return false; else exMap.put(block.ID,block.toJSON());
        current = block;
        currentHeight = block.height;
        blockchainMap.put(block.ID,block);
        heightMap.put(block.height,block);
        Miner.height = block.height.add(BigInteger.ONE);
        Miner.prevID = block.ID;
        saveBlock(block);
        csMap.put("current",block.toJSON());
        csMap.put("height",block.height.toString());
        csDB.commit();
        for(String ID:block.getTransactionKeys())
        {
            //System.out.println("ID for trans block is: " + ID);
            tmMap.put(ID,block.height.toString());
        }
        tmDB.commit();
        cmMap.put(block.ID,block.height.toString());
        cmDB.commit();
        Block b = verifyLater.get(block.height.add(BigInteger.ONE));
        if(b != null) {
            addBlock(b);
            verifyLater.remove(b.height);
        }
        ki.getTransMan().getUTXODB().commit();
        ki.getTransMan().getUTXOValueDB().commit();
        return true;
    }

    @Override
    public synchronized void loadChain() {
        if(csMap.get("height") == null)
        {
            return;
        }
        ki.getMainLog().info("current height is: " + csMap.get("height"));
        currentHeight = new BigInteger(csMap.get("height"));
        if(getByHeight(currentHeight) == null)
        {
            currentHeight = BigInteger.valueOf(-1);
            csMap.clear();
            tmMap.clear();
            cmMap.clear();
            csDB.commit();
            tmDB.commit();
            cmDB.commit();
        }

        Miner.height = currentHeight().add(BigInteger.ONE);
        if(getByHeight(currentHeight()) != null)
        Miner.prevID = getByHeight(currentHeight()).ID;

    }

    @Override
    public void saveChain() {
        //will probably delete soon
    }

    @Override
    public synchronized void clearFile() {
        File folder = new File("blocks/");
        File[] all = folder.listFiles();

        for(int i = 0; i < all.length; i++)
        {
            all[i].delete();
        }
    }

    @Override
    public synchronized void saveBlock(Block b) {
        StringFileHandler fh = new StringFileHandler(ki,folderName + b.height.divide(BigInteger.valueOf(16L)) + fileName);
        if(fh.getLines() == null || fh.getLines().isEmpty())
        {
            for(int i = 0; i < 16;i++)
            {
                fh.addLine("");
            }
            fh.save();
        }

        fh.replaceLine(b.height.mod(BigInteger.valueOf(16L)).intValueExact(),b.toJSON());

    }


    @Override
    public synchronized Map<String, Block> getChain() {
        return blockchainMap;
    }

    @Override
    public synchronized boolean softVerifyBlock(Block block) {

        Block current = getByHeight(block.height.subtract(BigInteger.ONE));
        boolean foundCoinbase = false;
        if(block.height.compareTo(currentHeight()) < 0)
        {
            //this is a "replacement" for an older block, we need the rest of the chain to verify this is actually part of it
            return false;
        }
        //ki.getMainLog().info("Forward moving block");
        if(block.getTransactionKeys().isEmpty()) return false; //WE NEED A COINBASE TRANSACTION FUCKER
        //ki.getMainLog().info("has transactions");
        //ki.getMainLog().info("Height is: " + block.height);
        if(current == null && block.height.compareTo(BigInteger.ZERO) != 0)
        {
            //we do not have the block before this one yet
            return false;
        }
        //ki.getMainLog().info("we have the block before this one");
        /*
        THIS SHIT BELONGS WITH THE NETWORK FOR NEW BLOCKS YOU MOTHER FUCKING ASSHOLE
        if(block.timestamp < System.currentTimeMillis() - 20000L) return false; //past dated block
        if(block.timestamp > System.currentTimeMillis() + 120000L) return false; //future dated block
        */
        //ki.getMainLog().info("ID is: " + block.ID);
        if(current != null && !block.prevID.equalsIgnoreCase(current.ID)) return false;
        String hash = EncryptionManager.sha256(block.header());
        //ki.getMainLog().info("Previous ID is ok");
        //ki.getMainLog().info("Hash is: " + hash);
        if(!block.ID.equals(hash)) return false;
        //ki.getMainLog().info("ID is ok");
        //System.out.println("checking difficulty");
        //System.out.println(EncryptionManager.sha256(block.header()));
        if(new BigInteger(Utils.toByteArray(hash)).abs().compareTo(currentDifficulty) > 0) return false;
        //ki.getMainLog().info("Solves for difficulty");

        for(String trans:block.getTransactionKeys())
        {
            if(block.getTransaction(trans).sender.equals("coinbase"))
            {
                //coinbase transaction
                MKiTransaction t = block.getTransaction(trans);
                BigInteger tFees = BigInteger.ZERO;
                for(String tFeeID:block.getTransactionKeys())
                {
                    tFees = tFees.add(block.getTransaction(tFeeID).transactionFee);
                }
                if(!t.receiver.equals(block.solver)) return false; //solver not on coinbase
                //ki.getMainLog().info("mark 1");
                if(t.amount.subtract(tFees).compareTo(blockRewardForHeight(block.height)) != 0) return false; // wrong reward
                //ki.getMainLog().info("mark 2");
                if(t.relayer != null) return false; //coinbase doesn't pay for relay
                //ki.getMainLog().info("mark 3");
                if(t.transactionFee.compareTo(BigInteger.ZERO) != 0) return false; //obviously no transaction fee as this is the block we're solving
                //ki.getMainLog().info("mark 4");
                if(t.relayFee.compareTo(BigInteger.ZERO) != 0) return false; //not allowing relay fee either for possible abuse
                //ki.getMainLog().info("mark 5");
                if(t.change.compareTo(BigInteger.ZERO) != 0) return false; //no change on coinbase
                //ki.getMainLog().info("mark 6");
                if(t.height.compareTo(block.height) != 0) return false; //for ease of lookup should be this blocks height
                //ki.getMainLog().info("mark 7");
                if(t.inputs != null) if(!t.inputs.isEmpty()) return false; //inputs should be null or empty
                //ki.getMainLog().info("mark 8");
                if(t.relaySignature != null) if(!t.relaySignature.isEmpty()) return false; //no relay, no sig
                //ki.getMainLog().info("mark 9");
                if(!ki.getEncryptMan().verifySig(t.all(),t.signature,t.receiver)) return false; //should be signed by solver once done
                //ki.getMainLog().info("mark 10");


                foundCoinbase = true;


            }else {
                if (!ki.getTransMan().softVerifyTransaction(block.getTransaction(trans))) return false;

            }
        }


        return foundCoinbase;
    }

    private synchronized int getCurrentSegment()
    {
        return currentHeight.divide(BigInteger.valueOf(1000L)).intValueExact();
    }

    private synchronized int getSegment(BigInteger height) {return height.subtract(BigInteger.ONE).divide(BigInteger.valueOf(1000L)).intValueExact(); }

    public synchronized boolean verifyBlock(Block block){

        Block current = getByHeight(block.height.subtract(BigInteger.ONE));
        boolean foundCoinbase = false;
        if(block.height.compareTo(currentHeight()) < 0)
        {
            //this is a "replacement" for an older block, we need the rest of the chain to verify this is actually part of it
            return false;
        }
        //ki.getMainLog().info("Forward moving block");
        if(block.getTransactionKeys().isEmpty()) return false; //WE NEED A COINBASE TRANSACTION FUCKER
        //ki.getMainLog().info("has transactions");
        //ki.getMainLog().info("Height is: " + block.height);
        if(current == null && block.height.compareTo(BigInteger.ZERO) != 0)
        {
            //we do not have the block before this one yet
            return false;
        }
        //ki.getMainLog().info("we have the block before this one");
        /*
        THIS SHIT BELONGS WITH THE NETWORK FOR NEW BLOCKS YOU MOTHER FUCKING ASSHOLE
        if(block.timestamp < System.currentTimeMillis() - 20000L) return false; //past dated block
        if(block.timestamp > System.currentTimeMillis() + 120000L) return false; //future dated block
        */
        //ki.getMainLog().info("ID is: " + block.ID);
        if(current != null && !block.prevID.equalsIgnoreCase(current.ID)) return false;
        String hash = EncryptionManager.sha256(block.header());
        //ki.getMainLog().info("Previous ID is ok");
        //ki.getMainLog().info("Hash is: " + hash);
        if(!block.ID.equals(hash)) return false;
        //ki.getMainLog().info("ID is ok");
        //System.out.println("checking difficulty");
        //System.out.println(EncryptionManager.sha256(block.header()));
        if(new BigInteger(Utils.toByteArray(hash)).abs().compareTo(currentDifficulty) > 0) return false;
        //ki.getMainLog().info("Solves for difficulty");

        for(String trans:block.getTransactionKeys())
        {
            if(block.getTransaction(trans).sender.equals("coinbase"))
            {
                //coinbase transaction
                MKiTransaction t = block.getTransaction(trans);
                BigInteger tFees = BigInteger.ZERO;
                for(String tFeeID:block.getTransactionKeys())
                {
                    tFees = tFees.add(block.getTransaction(tFeeID).transactionFee);
                }
                if(!t.receiver.equals(block.solver)) return false; //solver not on coinbase
                //ki.getMainLog().info("mark 1");
                if(t.amount.subtract(tFees).compareTo(blockRewardForHeight(block.height)) != 0) return false; // wrong reward
                //ki.getMainLog().info("mark 2");
                if(t.relayer != null) return false; //coinbase doesn't pay for relay
                //ki.getMainLog().info("mark 3");
                if(t.transactionFee.compareTo(BigInteger.ZERO) != 0) return false; //obviously no transaction fee as this is the block we're solving
                //ki.getMainLog().info("mark 4");
                if(t.relayFee.compareTo(BigInteger.ZERO) != 0) return false; //not allowing relay fee either for possible abuse
                //ki.getMainLog().info("mark 5");
                if(t.change.compareTo(BigInteger.ZERO) != 0) return false; //no change on coinbase
                //ki.getMainLog().info("mark 6");
                if(t.height.compareTo(block.height) != 0) return false; //for ease of lookup should be this blocks height
                //ki.getMainLog().info("mark 7");
                if(t.inputs != null) if(!t.inputs.isEmpty()) return false; //inputs should be null or empty
                //ki.getMainLog().info("mark 8");
                if(t.relaySignature != null) if(!t.relaySignature.isEmpty()) return false; //no relay, no sig
                //ki.getMainLog().info("mark 9");
                if(!ki.getEncryptMan().verifySig(t.all(),t.signature,t.receiver)) return false; //should be signed by solver once done
                //ki.getMainLog().info("mark 10");

                foundCoinbase = true;


            }else {
                if (!ki.getTransMan().softVerifyTransaction(block.getTransaction(trans))) return false;
            }
        }
        for(String trans:block.getTransactionKeys())
        {
            if(block.getTransaction(trans).sender.equals("coinbase"))
            {
                //coinbase transaction
                MKiTransaction t = block.getTransaction(trans);
                BigInteger tFees = BigInteger.ZERO;
                for(String tFeeID:block.getTransactionKeys())
                {
                    tFees = tFees.add(block.getTransaction(tFeeID).transactionFee);
                }

                ki.getTransMan().getUTXOValueMap().put(trans,t.amount.add(tFees).toString());
                ki.getTransMan().getUTXOMap().put(trans,block.height.toString());
                ki.getTransMan().getUTXOSpentMap().put(trans,false);
                if (ki.getTransMan().getUTXOMap().get(block.getTransaction(trans).receiver) != null) {
                    List<String> inputs = JSONManager.parseJSONToList(ki.getTransMan().getUTXOMap().get(block.getTransaction(trans).receiver));
                    inputs.add(trans);
                    ki.getTransMan().getUTXOMap().put(block.getTransaction(trans).receiver, JSONManager.parseListToJSON(inputs).toJSONString());
                } else {
                    List<String> inputs = new ArrayList<>();
                    inputs.add(trans);
                    ki.getTransMan().getUTXOMap().put(block.getTransaction(trans).receiver, JSONManager.parseListToJSON(inputs).toJSONString());

                }



            }else {
                if (!ki.getTransMan().verifyAndCommitTransaction(block.getTransaction(trans))) return false;
                else {
                    ki.getTransMan().getUTXOMap().put(trans, block.height.toString());
                    ki.getTransMan().getUTXOMap().put(trans + "change", block.height.toString());
                    ki.getTransMan().getUTXOMap().put(trans + "rfee", block.height.toString());
                    ki.getTransMan().getUTXOValueMap().put(trans, block.getTransaction(trans).amount.toString());
                    ki.getTransMan().getUTXOValueMap().put(trans + "change", block.getTransaction(trans).change.toString());
                    ki.getTransMan().getUTXOValueMap().put(trans + "rfee", block.getTransaction(trans).relayFee.toString());
                    ki.getTransMan().getUTXOSpentMap().put(trans,false);
                    ki.getTransMan().getUTXOSpentMap().put(trans + "change",false);
                    ki.getTransMan().getUTXOSpentMap().put(trans + "rfee",false);
                    //ki.getTransMan().getUTXOChangeValueMap().put(trans,block.getTransaction(trans).change.toString());

                    if (ki.getTransMan().getUTXOMap().get(block.getTransaction(trans).receiver) != null) {
                        List<String> inputs = JSONManager.parseJSONToList(ki.getTransMan().getUTXOMap().get(block.getTransaction(trans).receiver));
                        inputs.add(trans);
                        ki.getTransMan().getUTXOMap().put(block.getTransaction(trans).receiver, JSONManager.parseListToJSON(inputs).toJSONString());

                    } else {
                        List<String> inputs = new ArrayList<>();
                        inputs.add(trans);
                        ki.getTransMan().getUTXOMap().put(block.getTransaction(trans).receiver, JSONManager.parseListToJSON(inputs).toJSONString());
                    }
                    if (ki.getTransMan().getUTXOMap().get(block.getTransaction(trans).sender) != null) {
                        List<String> inputs = JSONManager.parseJSONToList(ki.getTransMan().getUTXOMap().get(block.getTransaction(trans).sender));
                        inputs.add(trans + "change");
                        ki.getTransMan().getUTXOMap().put(block.getTransaction(trans).sender, JSONManager.parseListToJSON(inputs).toJSONString());

                    } else {
                        List<String> inputs = new ArrayList<>();
                        inputs.add(trans + "change");
                        ki.getTransMan().getUTXOMap().put(block.getTransaction(trans).sender, JSONManager.parseListToJSON(inputs).toJSONString());
                    }
                    if (ki.getTransMan().getUTXOMap().get(block.getTransaction(trans).relayer) != null) {
                        List<String> inputs = JSONManager.parseJSONToList(ki.getTransMan().getUTXOMap().get(block.getTransaction(trans).relayer));
                        inputs.add(trans + "rfee");
                        ki.getTransMan().getUTXOMap().put(block.getTransaction(trans).relayer, JSONManager.parseListToJSON(inputs).toJSONString());

                    } else {
                        List<String> inputs = new ArrayList<>();
                        inputs.add(trans + "rfee");
                        ki.getTransMan().getUTXOMap().put(block.getTransaction(trans).relayer, JSONManager.parseListToJSON(inputs).toJSONString());
                    }


                }
            }
        }
        if(block.height.mod(BigInteger.valueOf(1000L)).equals(BigInteger.valueOf(0)) && block.height.compareTo(BigInteger.ZERO) != 0)
        {
            if(this.current != null) {
                recalculateDifficulty();
            }
        }
        return foundCoinbase;
    }

    public synchronized BigInteger currentHeight()
    {
        return currentHeight;
    }

    @Override
    public synchronized Block getByHeight(BigInteger height) {
       StringFileHandler fh = new StringFileHandler(ki,folderName + height.divide(BigInteger.valueOf(16L)) + fileName);

       String line = fh.getLine(height.mod(BigInteger.valueOf(16L)).intValueExact());
       if(line == null || line.isEmpty())
       {
           return null;
       }

       return Block.fromJSON(line);
    }

    public Block getBlock(long height)
    {
        return null;
    }

    private synchronized void recalculateDifficulty()
    {
        BigInteger correctDelta = BigInteger.valueOf(30000000L);
        BigInteger actualDelta = BigInteger.valueOf(getByHeight(currentHeight()).timestamp - getByHeight(currentHeight().subtract(BigInteger.valueOf(100L))).timestamp);
        BigInteger percentage = actualDelta.divide(correctDelta.divide(BigInteger.valueOf(10000000L)));
        currentDifficulty = currentDifficulty.multiply(percentage);
        currentDifficulty = currentDifficulty.divide(BigInteger.valueOf(10000000L));

        //currentDifficulty = currentDifficulty.multiply((BigInteger.valueOf(System.currentTimeMillis() - (currentHeight.intValueExact() * 300000L)).multiply(BigInteger.valueOf(100L))).divide(BigInteger.valueOf(GENESIS_DAY))).divide(BigInteger.valueOf(100L));
        ki.getMainLog().info("New Difficulty: " + Utils.toHexArray(currentDifficulty.toByteArray()));
    }


    public synchronized  BigInteger calculateDiff(BigInteger currentDifficulty, long timeElapsed)
    {

        BigInteger correctDelta = BigInteger.valueOf(300000000L);
        BigInteger actualDelta = BigInteger.valueOf(timeElapsed);
        BigInteger percentage = actualDelta.divide(correctDelta.divide(BigInteger.valueOf(100000000L)));
        currentDifficulty = currentDifficulty.multiply(percentage);
        currentDifficulty = currentDifficulty.divide(BigInteger.valueOf(100000000L));
        return currentDifficulty;
    }

    public static BigInteger blockRewardForHeight(BigInteger height)
    {
        if(height.equals(BigInteger.ZERO)) return BigInteger.valueOf(3800000000000000L);
        return BigInteger.valueOf(100L).multiply(BigInteger.valueOf(13934304L).subtract(height).multiply(BigInteger.valueOf(100000000L)).divide(BigInteger.valueOf(13934304L)));
    }

    //===============CHAIN IDS========================\\
    public static final short ANCHOR_CHAIN = 0x0101;
    public static final short TEST_NET_ANCHOR = 0x1101;
    public static final short POW_CHAIN = 0x0111;
    public static final short POS_CHAIN = 0x0222;
    public static final short TEST_NET_POW = 0x1111;
    public static final short TEST_NET_POS = 0x1222;

    private String toJSON()
    {
        JSONObject obj = new JSONObject();

        obj.put("segment",currentHeight.divide(BigInteger.valueOf(1000L)).toString());
        for(String ID:blockchainMap.keySet())
        {
            obj.put(ID,blockchainMap.get(ID).toJSON());
        }

        return obj.toJSONString();

    }

    private Map<BigInteger,Block> fromJSONheightOld(String json)
    {
        Map<String,String> map = JSONManager.parseJSONtoMap(json);
        map.remove("segment");
        Map<BigInteger,Block> bmap = new HashMap<>();
        for(String ID:map.keySet())
        {
            Block b = Block.fromJSON(map.get(ID));
            bmap.put(b.height,b);

        }

        return bmap;
    }

    @Override
    public synchronized BigInteger getCurrentDifficulty()
    {
        return currentDifficulty;
    }

    @Override
    public synchronized Block getByID(String ID) {
        if(cmMap.get(ID) == null) return null;
        return getByHeight(new BigInteger(cmMap.get(ID)));
    }

    @Override
    public synchronized void undoToBlock(String ID) {
        canMine = false;

        Block block = getByID(ID);

        currentHeight = block.height;
        csMap.put("current",block.toJSON());
        csMap.put("height",block.height.toString());
        csDB.commit();
        canMine = true;
    }

    @Override
    public boolean canMine() {
        return canMine;
    }

    @Override
    public void setCanMine(boolean canMine) {
        this.canMine = canMine;
    }

    @Override
    public synchronized void verifyLater(Block b) {

        verifyLater.put(b.height,b);

    }

    @Override
    public synchronized Block formEmptyBlock() {
        Block b = new Block();
        b.solver = Utils.toHexArray(ki.getEncryptMan().getPublicKey().getEncoded());
        b.timestamp = System.currentTimeMillis();
        for (String key : b.getTransactionKeys()) {

            if(ki.getTransMan().getUTXOSpentMap().containsKey(key))
            ki.getTransMan().getPending().remove(key);
        }

        b.addAll(ki.getTransMan().getPending());

        b.height = currentHeight().add(BigInteger.ONE);
        if (!(currentHeight().compareTo(BigInteger.valueOf(-1L)) == 0)) {
            if (getByHeight(currentHeight()) == null) ki.getMainLog().info("Current is null");
            b.prevID = getByHeight(currentHeight()).ID;
        } else
            b.prevID = "0";

        MKiTransaction coinbase = new MKiTransaction();
        coinbase.height = b.height;
        BigInteger tFees = BigInteger.ZERO;
        for (String tFeeID : b.getTransactionKeys()) {
            tFees = tFees.add(b.getTransaction(tFeeID).transactionFee);
        }
        coinbase.amount = ChainManager.blockRewardForHeight(b.height).add(tFees);
        coinbase.receiver = Utils.toHexArray(ki.getEncryptMan().getPublicKey().getEncoded());
        coinbase.relayFee = BigInteger.ZERO;
        coinbase.transactionFee = BigInteger.ZERO;
        coinbase.change = BigInteger.ZERO;

        coinbase.sender = "coinbase";
        coinbase.ID = EncryptionManager.sha256(coinbase.all());
        coinbase.signature = ki.getEncryptMan().sign(coinbase.all());
        b.addTransaction(coinbase);
        b.ID = EncryptionManager.sha256(b.header());

        return b;
    }

    private Map<String,Block> fromJSONOld(String json)
    {
        Map<String,String> map = JSONManager.parseJSONtoMap(json);
        map.remove("segment");
        Map<String,Block> bmap = new HashMap<>();
        for(String ID:map.keySet())
        {
            Block b = Block.fromJSON(map.get(ID));
            bmap.put(ID,b);

        }

        return bmap;
    }


    private void fromJSON(String json)
    {
        Map<String,String> map = JSONManager.parseJSONtoMap(json);
        map.remove("segment");
        for(String ID:map.keySet())
        {
            Block b = Block.fromJSON(map.get(ID));
            blockchainMap.put(ID, b);
            heightMap.put(b.height,b);
            if(currentHeight.compareTo(b.height) < 0)
            {
                currentHeight = b.height;
                current = b;
            }

        }
    }



}
