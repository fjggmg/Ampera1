package com.lifeform.main.blockchain;

import com.lifeform.main.IKi;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.JSONManager;
import com.lifeform.main.data.Utils;
import com.lifeform.main.data.files.StringFileHandler;
import com.lifeform.main.transactions.*;
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
    //TODO: WE NEED TO ACTUALLY USE THE CHAIN IDS AND ADD A TESTNET!
    private IKi ki;
    private boolean canMine = true;
    Block current;
    BigInteger currentDifficulty = new BigInteger("00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",16);
    BigInteger currentHeight = BigInteger.valueOf(-1L);
    DB csDB;
    DB tmDB;
    DB exDB;
    DB cmDB;
    //===============CHAIN IDS========================\\
    public static final short POW_CHAIN = 0x0001;
    public static final short TEST_NET= 0x1110;
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
    private String folderName;
    private short chainID;
    private boolean bDebug;

    public ChainManager(IKi ki, short chainID, String folderName, String csFile, String transFile, String extraFile, String cmFile, Block primer, boolean bDebug)
    {
        this(ki, chainID, folderName, csFile, transFile, extraFile, cmFile, bDebug);
        primeChain(primer);

    }

    public short getChainVer() {
        return chainID;
    }
    /**
     * ONLY FOR USE WITH TEMP CHAIN MANAGER FOR PRIMING CHAIN! NOT FOR USE WITH NORMAL CHAIN, FORCES BLOCK ONTO STACK
     * @param block
     */
    private synchronized void primeChain(Block block)
    {
        if (bDebug)
            ki.debug("Priming temp chain with block of height: " + block.height);
        current = block;
        currentHeight = block.height;
        blockchainMap.put(block.ID,block);
        heightMap.put(block.height,block);

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
    }

    /**
     * PoW system only
     * @param chainID
     */
    public ChainManager(IKi ki, short chainID, String folderName, String csFile, String transFile, String extraFile, String cmFile, boolean bDebug)
    {
        this.ki = ki;
        this.folderName = chainID + folderName;
        this.chainID = chainID;
        this.bDebug = bDebug;
        new File("chain" + chainID + "/").mkdirs();
        csDB = DBMaker.fileDB("chain" + chainID + "/" + csFile).fileMmapEnableIfSupported().transactionEnable().make();

        csMap = csDB.hashMap("csDB", Serializer.STRING,Serializer.STRING).createOrOpen();

        tmDB = DBMaker.fileDB("chain" + chainID + "/" + transFile).fileMmapEnableIfSupported().transactionEnable().make();

        tmMap = tmDB.hashMap("tmDB",Serializer.STRING,Serializer.STRING).createOrOpen();

        exDB = DBMaker.fileDB("chain" + chainID + "/" + extraFile).fileMmapEnableIfSupported().transactionEnable().make();

        exMap = exDB.hashMap("exDB",Serializer.STRING,Serializer.STRING).createOrOpen();

        cmDB = DBMaker.fileDB("chain" + chainID + "/" + cmFile).fileMmapEnableIfSupported().transactionEnable().make();

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
        ki.blockTick(block);
        current = block;
        currentHeight = block.height;
        blockchainMap.put(block.ID,block);
        heightMap.put(block.height,block);

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

        return true;
    }

    @Override
    public synchronized void loadChain() {
        if(csMap.get("height") == null)
        {
            return;
        }
        //ki.getMainLog().info("current height is: " + csMap.get("height"));
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
        CPUMiner.height = currentHeight().add(BigInteger.ONE);
        if(getByHeight(currentHeight()) != null)
            CPUMiner.prevID = getByHeight(currentHeight()).ID;
        if(csMap.get("diff") == null)
        {
            return;
        }
        currentDifficulty = new BigInteger(csMap.get("diff"));



    }

    @Override
    public void saveChain() {
        //will probably delete soon
    }

    @Override
    public synchronized void clearFile() {
        File folder = new File(folderName);
        File[] all = folder.listFiles();

        for(int i = 0; i < all.length; i++)
        {
            all[i].delete();
        }
        csMap.clear();
        tmMap.clear();
        cmMap.clear();
        csDB.commit();
        tmDB.commit();
        cmDB.commit();
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
        if (bDebug)
            ki.debug("verifying block...");
        if(block.height.compareTo(currentHeight()) < 0)
        {
            //this is a "replacement" for an older block, we need the rest of the chain to verify this is actually part of it
            return false;
        }
        if (bDebug)
            ki.debug("Height is ok");

        if(current == null && block.height.compareTo(BigInteger.ZERO) != 0)
        {
            return false;
        }
        if (bDebug)
            ki.debug("Height check 2 is ok");
        if(current != null && !block.prevID.equalsIgnoreCase(current.ID)) return false;
        if (bDebug)
            ki.debug("prev ID is ok");
        String hash = EncryptionManager.sha512(block.header());
        if(!block.ID.equals(hash)) return false;
        if (bDebug)
            ki.debug("ID is ok");
        if(new BigInteger(Utils.fromBase64(hash)).abs().compareTo(currentDifficulty) > 0) return false;
        if (bDebug)
            ki.debug("Solves for difficulty");
        if(block.getCoinbase() == null) return false;
        if (bDebug)
            ki.debug("coinbase is in block");
        BigInteger fees = BigInteger.ZERO;

        for(String t:block.getTransactionKeys())
        {
            fees = fees.add(block.getTransaction(t).getFee());
        }

        if(!ki.getTransMan().verifyCoinbase(block.getCoinbase(),block.height,fees)) return false;
        if (bDebug)
            ki.debug("Coinbase verifies ok");
        BlockVerificationHelper bvh = new BlockVerificationHelper(ki, block);
        if (!bvh.verifyTransactions()) return false;
        List<String> inputs = new ArrayList<>();
        for(String t: block.getTransactionKeys())
        {
            //if(!ki.getTransMan().verifyTransaction(block.getTransaction(t))) return false;
            for(Input i:block.getTransaction(t).getInputs())
            {
                if(inputs.contains(i.getID())) return false; else inputs.add(i.getID());
            }
        }
        if (bDebug)
            ki.debug("transactions verify ok");
        return true;
    }

    private synchronized int getCurrentSegment()
    {
        return currentHeight.divide(BigInteger.valueOf(1000L)).intValueExact();
    }

    private synchronized int getSegment(BigInteger height) {return height.subtract(BigInteger.ONE).divide(BigInteger.valueOf(1000L)).intValueExact(); }

    public synchronized boolean verifyBlock(Block block){
        if (bDebug)
        ki.getMainLog().info("Block has: " + block.getTransactionKeys().size() + " transactions");
        if(!softVerifyBlock(block)) return false;
        BigInteger fees = BigInteger.ZERO;

        for(String t:block.getTransactionKeys())
        {
            fees = fees.add(block.getTransaction(t).getFee());
        }

        if(!ki.getTransMan().addCoinbase(block.getCoinbase(),block.height,fees)) return false;

        //IBlockVerificationHelper bvh = new BlockVerificationHelper(ki,block);
        //if(!bvh.addTransactions()) return false;

        for(String key:block.getTransactionKeys())
        {
            if (!ki.getTransMan().addTransactionNoVerify(block.getTransaction(key))) return false;
        }

        ki.getTransMan().commit();
        if(block.height.mod(BigInteger.valueOf(1000L)).equals(BigInteger.valueOf(0)) && block.height.compareTo(BigInteger.ZERO) != 0)
        {
            if(this.current != null) {
                recalculateDifficulty();
            }
        }

        return true;
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



    private synchronized void recalculateDifficulty()
    {
        BigInteger correctDelta = BigInteger.valueOf(300000000L);
        BigInteger actualDelta = BigInteger.valueOf(getByHeight(currentHeight()).timestamp - getByHeight(currentHeight().subtract(BigInteger.valueOf(1000L))).timestamp);
        BigInteger percentage = actualDelta.divide(correctDelta.divide(BigInteger.valueOf(100000000L)));
        currentDifficulty = currentDifficulty.multiply(percentage);
        currentDifficulty = currentDifficulty.divide(BigInteger.valueOf(100000000L));
        csMap.put("diff",currentDifficulty.toString());

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
        if(height.equals(BigInteger.ZERO)) return BigInteger.valueOf(4000000000000000L);
        return BigInteger.valueOf(100L).multiply(BigInteger.valueOf(13934304L).subtract(height).multiply(BigInteger.valueOf(100000000L)).divide(BigInteger.valueOf(13934304L)));
    }



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

    @Deprecated
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
        b.solver = ki.getEncryptMan().getPublicKeyString();
        b.timestamp = System.currentTimeMillis();

        Map<String,ITrans> transactions = new HashMap<>();
        for(ITrans trans:ki.getTransMan().getPending())
        {
            transactions.put(trans.getID(),trans);
        }
        b.addAll(transactions);

        b.height = currentHeight().add(BigInteger.ONE);
        //if(b.height.compareTo(BigInteger.ZERO) == 0) b.height = b.height.add(BigInteger.ONE);
        if (!(currentHeight().compareTo(BigInteger.valueOf(-1L)) == 0)) {
            if (getByHeight(currentHeight()) == null) ki.getMainLog().info("Current is null");
            b.prevID = getByHeight(currentHeight()).ID;
        } else
            b.prevID = "0";

        BigInteger fees = BigInteger.ZERO;
        for(String t:transactions.keySet())
        {
            fees = fees.add(transactions.get(t).getFee());
        }
        Output o = new Output(blockRewardForHeight(currentHeight().add(BigInteger.ONE)).add(fees),ki.getAddMan().getMainAdd(), Token.ORIGIN,0, System.currentTimeMillis());
        List<Output> outputs = new ArrayList<>();
        outputs.add(o);
        int i = 1;
        if(b.height.compareTo(BigInteger.ZERO) == 0)
        {
            for(Token t:Token.values())
            {
                if(!t.equals(Token.ORIGIN)) {
                    outputs.add(new Output(BigInteger.valueOf(Long.MAX_VALUE), ki.getAddMan().getMainAdd(), t, i, System.currentTimeMillis()));
                    i++;
                }
            }
        }
        ITrans coinbase = new Transaction("coinbase",0,new HashMap<String,String>(),outputs,new ArrayList<Input>(),new HashMap<>(),new ArrayList<String>());

        b.setCoinbase(coinbase);
        //b.addTransaction(coinbase);
        b.ID = EncryptionManager.sha512(b.header());

        return b;
    }

    @Deprecated
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


    @Deprecated
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