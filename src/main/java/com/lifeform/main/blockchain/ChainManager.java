package com.lifeform.main.blockchain;

import com.lifeform.main.IKi;
import com.lifeform.main.Ki;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.JSONManager;
import com.lifeform.main.data.Utils;
import com.lifeform.main.data.XodusStringMap;
import com.lifeform.main.data.files.StringFileHandler;
import com.lifeform.main.transactions.*;
import org.json.simple.JSONObject;

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
    private Block current;
    private BigInteger currentDifficulty = new BigInteger("00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
    private volatile BigInteger currentHeight = BigInteger.valueOf(-1L);
    //DB csDB;
    //DB tmDB;
    //DB exDB;
    //DB cmDB;
    private XodusStringMap blockHeightMap;
    private XodusStringMap blockIDMap;
    private Block tempBlock = null;
    //===============CHAIN IDS========================\\
    public static final short POW_CHAIN = 0x0001;
    public static final short TEST_NET= 0x1110;
    private Map<BigInteger,Block> verifyLater = new HashMap<>();
    /**
     * stupid way to store the chain for easy access momentarily
     */
    Map<String, Block> blockchainMap = new HashMap<>();
    Map<BigInteger,Block> heightMap = new HashMap<>();
    XodusStringMap csMap;
    //ConcurrentMap<String,String> tmMap;
    //ConcurrentMap<String,String> exMap;
    //ConcurrentMap<String,String> cmMap;
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
    private synchronized void primeChain(Block block) {
        if (bDebug)
            ki.debug("Priming temp chain with block of height: " + block.height);
        current = block;
        currentHeight = block.height;
        blockchainMap.put(block.ID, block);
        heightMap.put(block.height, block);

        saveBlock(block);
        csMap.put("current", block.toJSON());
        csMap.put("height", block.height.toString());
    }

    public Block getTemp() {
        return tempBlock;
    }

    public void setTemp(Block b) {
        tempBlock = b;
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

        csMap = new XodusStringMap("chain" + chainID + "/" + csFile + "xodus");

        blockHeightMap = new XodusStringMap(folderName + chainID + "heights");
        blockIDMap = new XodusStringMap(folderName + chainID + "IDs");

    }

    public void close()
    {
        csMap.close();
    }

    public synchronized BlockState addBlock(Block block) {
        Ki.canClose = false;
        BlockState state = verifyBlock(block);
        if (!state.success()) {
            Ki.canClose = true;
            return state;
        }


        current = block;
        currentHeight = block.height;
        blockchainMap.put(block.ID,block);
        heightMap.put(block.height,block);

        saveBlock(block);
        csMap.put("current",block.toJSON());
        csMap.put("height",block.height.toString());
        if (block.height.mod(BigInteger.valueOf(1000L)).equals(BigInteger.ZERO) && block.height.compareTo(BigInteger.ZERO) != 0) {
            if (this.current != null) {
                recalculateDifficulty();
            }
        }
        /*
        Block b = verifyLater.get(block.height.add(BigInteger.ONE));
        if(b != null) {
            addBlock(b);
            verifyLater.remove(b.height);
        }
        */
        Ki.canClose = true;
        if (ki.getMinerMan() != null && ki.getMinerMan().isMining()) {
            ki.debug("Restarting miners");
            /* old miner stuff
             CPUMiner.height = ki.getChainMan().currentHeight().add(BigInteger.ONE);
             CPUMiner.prevID = ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).ID;
             */

            ki.getMinerMan().restartMiners();
        }
        if (ki.getOptions().poolRelay) {
            int i = 128 - currentDifficulty.toString(16).length();

            ki.getPoolManager().updateCurrentHeight(ki.getChainMan().currentHeight());
            ki.getPoolManager().updateCurrentPayPerShare((long) ((Math.pow(16, 8) / Math.pow(16, i) * blockRewardForHeight(currentHeight()).longValueExact()) * 0.99));
        }
        ki.blockTick(block);
        return BlockState.SUCCESS;
    }

    @Override
    public void setHeight(BigInteger height) {
        this.currentHeight = height;
        csMap.put("height", height.toString());

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

        }
        /* old miner
        CPUMiner.height = currentHeight().add(BigInteger.ONE);
        if(getByHeight(currentHeight()) != null)
            CPUMiner.prevID = getByHeight(currentHeight()).ID;
         */
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
    }


    @Override
    public void saveBlock(Block b) {


        blockHeightMap.put(b.height.toString(), b.toJSON());
        blockIDMap.put(b.ID, b.toJSON());
        /*
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
        */
    }


    @Override
    public synchronized Map<String, Block> getChain() {
        return blockchainMap;
    }

    //TODO: removing synchronized on this method as it appears to be locking up sometimes during mining, see what affect this has elsewhere
    @Override
    public BlockState softVerifyBlock(Block block) {

        Block current = getByHeight(block.height.subtract(BigInteger.ONE));
        if (bDebug)
            ki.debug("verifying block...");
        if(block.height.compareTo(currentHeight()) < 0) {
            //this is a "replacement" for an older block, we need the rest of the chain to verify this is actually part of it
            return BlockState.WRONG_HEIGHT;
        }
        if (bDebug)
            ki.debug("Height is ok");


        if (current == null) {
            if (block.height.compareTo(BigInteger.ZERO) != 0) {
                ki.debug("Height is not 0 and current block is null, bad block");
                return BlockState.NO_PREVIOUS;
            }
        }


        if (bDebug)
            ki.debug("Height check 2 is ok");
        if (current != null && !block.prevID.equalsIgnoreCase(current.ID)) return BlockState.PREVID_MISMATCH;

        if (bDebug)
            ki.debug("prev ID is ok");
        if (current != null && block.timestamp < current.timestamp) return BlockState.BACKWARDS_TIMESTAMP;
        //TODO: check this shit out, timestamps have been fucking us since day 1
        if (block.timestamp > System.currentTimeMillis() + 60000L) return BlockState.TIMESTAMP_WRONG;
        if (bDebug)
            ki.debug("timestamp is OK");
        String hash = EncryptionManager.sha512(block.header());
        if (!block.ID.equals(hash)) return BlockState.ID_MISMATCH;
        if (bDebug)
            ki.debug("ID is ok");
        byte[] bigIntDiffByteArray = currentDifficulty.toByteArray();
        byte[] byteDiff = new byte[64];
        int p = 63;
        int k = bigIntDiffByteArray.length - 1;
        while (k >= 0) {
            byteDiff[p] = bigIntDiffByteArray[k];
            k--;
            p--;
        }

        int mostSignificant0Digits = 0;
        for (int i = 0; i < byteDiff.length; i++) {
            mostSignificant0Digits = i;
            if (byteDiff[i] != 0) {
                break;
            }
        }
        int mostSignificantByte = byteDiff[mostSignificant0Digits] & 0x0ff;

        byte[] byteHash = Utils.fromBase64(hash);

        int precedingZeroes = 0;
        for (int i = 0; i < mostSignificant0Digits; i++) {
            precedingZeroes = (precedingZeroes | byteHash[i]) & 0x00ff;
        }

        if (!(precedingZeroes == 0 && byteHash[mostSignificant0Digits] <= mostSignificantByte)) {
            return BlockState.NO_SOLVE;
        }
        if (bDebug)
            ki.debug("Solves for difficulty");
        if (block.getCoinbase() == null) return BlockState.NO_COINBASE;
        if (bDebug)
            ki.debug("coinbase is in block");
        BigInteger fees = BigInteger.ZERO;

        for(String t:block.getTransactionKeys()) {
            fees = fees.add(block.getTransaction(t).getFee());
        }

        if (!ki.getTransMan().verifyCoinbase(block.getCoinbase(), block.height, fees)) return BlockState.BAD_COINBASE;
        if (bDebug)
            ki.debug("Coinbase verifies ok");
        BlockVerificationHelper bvh = new BlockVerificationHelper(ki, block);
        if (!bvh.verifyTransactions()) return BlockState.BAD_TRANSACTIONS;
        List<String> inputs = new ArrayList<>();
        for(String t: block.getTransactionKeys()) {
            //if(!ki.getTransMan().verifyTransaction(block.getTransaction(t))) return false;
            for(Input i:block.getTransaction(t).getInputs()) {
                if (inputs.contains(i.getID())) return BlockState.DOUBLE_SPEND;
                else inputs.add(i.getID());
            }
        }
        if (bDebug)
            ki.debug("transactions verify ok");
        return BlockState.SUCCESS;
    }

    private synchronized int getCurrentSegment()
    {
        return currentHeight.divide(BigInteger.valueOf(1000L)).intValueExact();
    }

    private synchronized int getSegment(BigInteger height) {return height.subtract(BigInteger.ONE).divide(BigInteger.valueOf(1000L)).intValueExact(); }

    public BlockState verifyBlock(Block block) {
        if (bDebug)
            ki.debug("Block has: " + block.getTransactionKeys().size() + " transactions");
        BlockState state = softVerifyBlock(block);
        if (!state.success()) return state;
        BigInteger fees = BigInteger.ZERO;

        for(String t:block.getTransactionKeys())
        {
            fees = fees.add(block.getTransaction(t).getFee());
        }

        if (!ki.getTransMan().addCoinbase(block.getCoinbase(), block.height, fees))
            return BlockState.FAILED_ADD_COINBASE;

        //IBlockVerificationHelper bvh = new BlockVerificationHelper(ki,block);
        //if(!bvh.addTransactions()) return false;

        for(String key:block.getTransactionKeys())
        {
            if (!ki.getTransMan().addTransactionNoVerify(block.getTransaction(key))) return BlockState.FAILED_ADD_TRANS;
        }

        ki.getTransMan().commit();
        //setHeight(block.height);

        if (!ki.getOptions().nogui) {
            if (ki.getEncryptMan().getPublicKeyString().equals(block.solver)) {
                while (ki.getGUIHook() == null) {
                }
                ki.getGUIHook().blockFound();
            }
            for (String trans : block.getTransactionKeys()) {
                boolean add = false;
                ITrans transaction = block.getTransaction(trans);
                for (Output o : transaction.getOutputs()) {
                    for (Address a : ki.getAddMan().getActive()) {
                        if (o.getAddress().encodeForChain().equals(a.encodeForChain())) {
                            add = true;
                        }
                    }
                }
                for (Input i : transaction.getInputs()) {
                    for (Address a : ki.getAddMan().getActive()) {
                        if (i.getAddress().encodeForChain().equals(a.encodeForChain())) {
                            add = true;
                        }
                    }
                }
                if (add) {
                    ki.getGUIHook().addTransaction(transaction, block.height);
                }
            }
        }

        return BlockState.SUCCESS;
    }

    public BigInteger currentHeight()
    {
        return currentHeight;
    }

    @Override
    public synchronized Block getByHeight(BigInteger height) {

        if (useCache) {
            if (cache.containsKey(height)) {
                return cache.get(height);
            }
        }
        if (blockHeightMap.get(height.toString()) == null) return null;

        return Block.fromJSON(blockHeightMap.get(height.toString()));

        /*
       StringFileHandler fh = new StringFileHandler(ki,folderName + height.divide(BigInteger.valueOf(16L)) + fileName);

       String line = fh.getLine(height.mod(BigInteger.valueOf(16L)).intValueExact());
       if(line == null || line.isEmpty())
       {
           return null;
       }

       return Block.fromJSON(line);*/
    }


    private void recalculateDifficulty()
    {
        BigInteger correctDelta = BigInteger.valueOf(300000000L);
        if (getByHeight(currentHeight()) == null) ki.debug("current height was null");
        if (getByHeight(currentHeight().subtract(BigInteger.valueOf(1000L))) == null) ki.debug("last 1000 was null");
        BigInteger actualDelta = BigInteger.valueOf(getByHeight(currentHeight().subtract(BigInteger.ONE)).timestamp - getByHeight(currentHeight().subtract(BigInteger.valueOf(1000L))).timestamp);
        BigInteger percentage = actualDelta.divide(correctDelta.divide(BigInteger.valueOf(100000000L)));
        currentDifficulty = currentDifficulty.multiply(percentage);
        currentDifficulty = currentDifficulty.divide(BigInteger.valueOf(100000000L));
        csMap.put("diff",currentDifficulty.toString());

        //currentDifficulty = currentDifficulty.multiply((BigInteger.valueOf(System.currentTimeMillis() - (currentHeight.intValueExact() * 300000L)).multiply(BigInteger.valueOf(100L))).divide(BigInteger.valueOf(GENESIS_DAY))).divide(BigInteger.valueOf(100L));
        ki.getMainLog().info("New Difficulty: " + Utils.toHexArray(currentDifficulty.toByteArray()));
        if (ki.getMinerMan() != null && ki.getMinerMan().miningCompatible() && ki.getOptions().mining) {
            boolean isMining = false;
            if (ki.getMinerMan().isMining()) {
                ki.getMinerMan().stopMiners();
                isMining = true;
            }
            try {
                ki.getMinerMan().setup();
            } catch (Exception e) {
                ki.debug("Re-initializing miners failed after difficulty recalcuation, message: " + e.getMessage());
            }
            if (isMining)
                ki.getMinerMan().startMiners();
        }
    }


    public BigInteger calculateDiff(BigInteger currentDifficulty, long timeElapsed)
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
    public BigInteger getCurrentDifficulty()
    {
        return currentDifficulty;
    }

    @Override
    public synchronized Block getByID(String ID) {

        if (blockIDMap.get(ID) == null) return null;
        return Block.fromJSON(blockIDMap.get(ID));
        /*
        if(cmMap.get(ID) == null) return null;
        return getByHeight(new BigInteger(cmMap.get(ID)));
        */
    }

    @Override
    public synchronized void undoToBlock(String ID) {
        canMine = false;

        Block block = getByID(ID);

        currentHeight = block.height;
        csMap.put("current",block.toJSON());
        csMap.put("height",block.height.toString());
        canMine = true;
    }

    @Override
    public synchronized void undoToBlock(BigInteger height) {
        BigInteger h1 = new BigInteger(height.toByteArray());
        for (; h1.compareTo(currentHeight()) <= 0; h1 = h1.add(BigInteger.ONE)) {
            for (String trans : getByHeight(h1).getTransactionKeys()) {
                ki.getTransMan().undoTransaction(ki.getChainMan().getByHeight(h1).getTransaction(trans));
            }
        }
        setHeight(height);
    }

    @Override
    public void setDiff(BigInteger diff) {
        currentDifficulty = diff;
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
    public void verifyLater(Block b) {

        verifyLater.put(b.height,b);

    }

    @Override
    public Block formEmptyBlock(BigInteger minFee) {
        Block b = new Block();
        b.solver = ki.getEncryptMan().getPublicKeyString();
        b.timestamp = System.currentTimeMillis();

        Map<String,ITrans> transactions = new HashMap<>();
        for(ITrans trans:ki.getTransMan().getPending())
        {
            if (trans.getFee().compareTo(minFee) >= 0)
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
        ITrans coinbase = new Transaction("coinbase", 0, new HashMap<String, String>(), outputs, new ArrayList<Input>(), new HashMap<>(), new ArrayList<String>(), TransactionType.STANDARD);

        b.setCoinbase(coinbase);
        //b.addTransaction(coinbase);
        b.ID = EncryptionManager.sha512(b.header());

        return b;
    }

    Map<BigInteger, Block> cache = new HashMap<>();
    private boolean useCache = false;

    @Override
    public synchronized void startCache(BigInteger height) {
        cache.clear();
        for (; height.compareTo(currentHeight) <= 0; height = height.add(BigInteger.ONE)) {
            cache.put(height, getByHeight(height));
        }
        useCache = true;
    }

    public synchronized void stopCache() {
        cache.clear();
        useCache = false;
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