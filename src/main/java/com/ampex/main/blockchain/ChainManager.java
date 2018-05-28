package com.ampex.main.blockchain;

import com.ampex.amperabase.*;
import com.ampex.main.IKi;
import com.ampex.main.Settings;
import com.ampex.main.StringSettings;
import com.ampex.main.blockchain.mining.GPUMiner;
import com.ampex.main.data.encryption.EncryptionManager;
import com.ampex.main.data.utils.Utils;
import com.ampex.main.data.xodus.XodusStringMap;
import com.ampex.main.transactions.*;
import database.XodusAmpMap;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bryan on 5/28/2017.
 *
 * Block time = 5m
 * default diff is 5 0 prefix to 512 bit hash
 *
 */
public class ChainManager implements IChainMan {

    private IKi ki;
    private boolean canMine = true;
    private Block current;
    private BigInteger currentDifficulty = new BigInteger("00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
    private volatile BigInteger currentHeight = BigInteger.valueOf(-1L);

    private XodusStringMap blockHeightMap;
    private XodusStringMap blockIDMap;
    private XodusAmpMap blockHeightAmp;
    private Block tempBlock = null;
    //===============CHAIN IDS========================\\
    public static final short POW_CHAIN = 0x0001;
    public static final short TEST_NET= 0x1110;
    private XodusStringMap csMap;
    private String folderName;
    private short chainID;
    private boolean bDebug;

    public ChainManager(IKi ki, short chainID, String folderName, String csFile, Block primer, boolean bDebug)
    {
        this(ki, chainID, folderName, csFile, bDebug);
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
     * Full node chain management system
     * @param ki god object reference
     * @param chainID which chain we are on (testnet or mainnet)
     * @param folderName folder where blocks are kept
     * @param csFile file name where chainstate vars are kept
     * @param bDebug true to debug here, false to not
     */
    public ChainManager(IKi ki, short chainID, String folderName, String csFile, boolean bDebug)
    {
        this.ki = ki;
        this.folderName = chainID + folderName;
        this.chainID = chainID;
        this.bDebug = bDebug;
        if (!new File("chain" + chainID + "/").mkdirs()) {
            ki.getMainLog().warn("Unable to create chain folder");
        }

        csMap = new XodusStringMap("chain" + chainID + "/" + csFile + "xodus");
        blockHeightAmp = new XodusAmpMap(folderName + chainID + "ampHeight");
        blockHeightMap = new XodusStringMap(folderName + chainID + "heights");
        blockIDMap = new XodusStringMap(folderName + chainID + "IDs");
        if (csMap.get("amp") == null && csMap.get("height") != null) {
            BigInteger height = BigInteger.ZERO;
            BigInteger chainHeight = new BigInteger(csMap.get("height"));
            while (height.compareTo(chainHeight) <= 0) {
                ki.debug("Deserializing block: " + height);
                Block b = Block.fromJSON(blockHeightMap.get(height.toString()));
                ki.debug("Serializing block: " + height);
                ki.debug("Size on map should be: " + b.serializeToAmplet().serializeToBytes().length);
                if (!blockHeightAmp.put(b.height.toByteArray(), b)) {
                    ki.debug("Not valid amplet byte array size: " + b.serializeToAmplet().serializeToBytes().length);
                } else {
                    try {

                        ki.debug("Size on map: " + blockHeightAmp.getBytes(b.height.toByteArray()).length);
                        ki.debug("Number of transactions: " + b.getTransactionKeys().size());
                        ki.debug("Value on map: " + Block.fromAmplet(blockHeightAmp.get(b.height.toByteArray())).ID);
                    } catch (Exception e) {
                        ki.getMainLog().error("Error reading data back", e);
                        ki.debug("Last block size: " + blockHeightAmp.getBytes(b.height.subtract(BigInteger.ONE).toByteArray()).length);

                    }
                }
                BigInteger fees = BigInteger.ZERO;

                for (String t : b.getTransactionKeys()) {
                    fees = fees.add(b.getTransaction(t).getFee());
                }
                ki.getTransMan().addCoinbase(b.getCoinbase(), b.height, fees);
                for (String t : b.getTransactionKeys()) {
                    if (!ki.getTransMan().addTransaction(b.getTransaction(t))) {
                        ki.debug("Unable to add transaction from block during conversion. shutting down");
                        ki.close();
                    }
                }
                ki.getTransMan().postBlockProcessing(b);
                height = height.add(BigInteger.ONE);
            }
            blockHeightMap.clear();
            blockIDMap.clear();
        }
        csMap.put("amp", "amp");

    }

    public void close()
    {
        csMap.close();
        blockHeightMap.close();
        blockIDMap.close();
        blockHeightAmp.close();
    }

    public synchronized BlockState addBlock(Block block) {
        synchronized (ki.getCloseLock()) {
            ki.debug("Block data: ");
            ki.debug("Solver: " + block.solver);
            BlockState state = verifyBlock(block);
            if (!state.success()) {
                return state;
            }
            current = block;
            currentHeight = block.height;
            saveBlock(block);
            csMap.put("current", block.toJSON());
            csMap.put("height", block.height.toString());
            if (block.height.mod(BigInteger.valueOf(1000L)).equals(BigInteger.ZERO) && block.height.compareTo(BigInteger.ZERO) != 0) {
                if (this.current != null) {
                    recalculateDifficulty();
                }
            }

        }
        if (ki.getMinerMan() != null && ki.getMinerMan().isMining()) {
            ki.debug("Restarting miners");
            ki.getMinerMan().restartMiners();
        }
        if (ki.getOptions().poolRelay) {
            BigDecimal sd = new BigDecimal(GPUMiner.shareDiff);
            BigDecimal cd = new BigDecimal(ki.getChainMan().getCurrentDifficulty());
            ki.getPoolManager().updateCurrentHeight(ki.getChainMan().currentHeight());
            if (ki.getSetting(Settings.DYNAMIC_FEE)) {
                double fee;
                try {
                    fee = Double.parseDouble(ki.getStringSetting(StringSettings.POOL_FEE));
                } catch (Exception e) {
                    fee = 1;
                }
                ki.getPoolManager().updateCurrentPayPerShare((long) ((((cd.divide(sd, 9, RoundingMode.HALF_DOWN).doubleValue() * ChainManager.blockRewardForHeight(ki.getChainMan().currentHeight()).longValueExact()) * 1 - (fee / 100)))));
            } else {
                long pps;
                try {
                    pps = Long.parseLong(ki.getStringSetting(StringSettings.POOL_STATIC_PPS));
                } catch (Exception e) {
                    pps = 100;
                }
                ki.getPoolManager().updateCurrentPayPerShare(pps);
            }
        }
        ki.blockTick(block);
        ki.getTransMan().postBlockProcessing(block);
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
        currentHeight = new BigInteger(csMap.get("height"));
        if(getByHeight(currentHeight) == null)
        {
            currentHeight = BigInteger.valueOf(-1);
            csMap.clear();

        }
        if(csMap.get("diff") == null)
        {
            return;
        }
        currentDifficulty = new BigInteger(csMap.get("diff"));
        ki.getTransMan().setCurrentHeight(currentHeight);
    }

    @Override
    public synchronized void clearFile() {
        File folder = new File(folderName);
        File[] all = folder.listFiles();
        if (all == null) return;
        for (File anAll : all) {
            if (!anAll.delete()) {
                ki.getMainLog().warn("Unable to delete chain files");
            }
        }
        csMap.clear();
    }


    @Override
    public void saveBlock(Block b) {
        blockHeightAmp.put(b.height.toByteArray(), b);
    }

    @Override
    public synchronized BlockState softVerifyBlock(Block block) {

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

        if (block.timestamp > System.currentTimeMillis() + 60000L) return BlockState.TIMESTAMP_WRONG;
        if (bDebug)
            ki.debug("timestamp is OK");
        String hash = EncryptionManager.sha512(block.header());
        if (!block.ID.equals(hash)) return BlockState.ID_MISMATCH;
        if (bDebug)
            ki.debug("ID is ok");
        int txios = 0;
        for (String trans : block.getTransactionKeys()) {
            txios += block.getTransaction(trans).getOutputs().size();
            txios += block.getTransaction(trans).getInputs().size();
            if (txios > IChainMan.MAX_TXIOS) return BlockState.BAD_TRANSACTIONS;
        }
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
        int mostSignificantByte = byteDiff[mostSignificant0Digits] & 0x0000ff;

        byte[] byteHash = Utils.fromBase64(hash);

        int precedingZeroes = 0;
        for (int i = 0; i < mostSignificant0Digits; i++) {
            precedingZeroes = (precedingZeroes | byteHash[i]) & 0x00ff;
        }
        if (block.height.compareTo(BigInteger.valueOf(32910L)) < 0) {
            if (!(precedingZeroes == 0 && byteHash[mostSignificant0Digits] <= mostSignificantByte)) {
                return BlockState.NO_SOLVE;
            }
        } else {
            if (!(precedingZeroes == 0 && ((byteHash[mostSignificant0Digits] & 0x00ff) <= mostSignificantByte))) {
                return BlockState.NO_SOLVE;
            }
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
            for (IInput i : block.getTransaction(t).getInputs()) {
                if (inputs.contains(i.getID())) return BlockState.DOUBLE_SPEND;
                else inputs.add(i.getID());
            }
        }
        if (bDebug)
            ki.debug("transactions verify ok");
        return BlockState.SUCCESS;
    }

    public static boolean checkSolve(BigInteger currentDifficulty, BigInteger height, String hash) {
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
        int mostSignificantByte = byteDiff[mostSignificant0Digits] & 0x0000ff;

        byte[] byteHash = Utils.fromBase64(hash);

        int precedingZeroes = 0;
        for (int i = 0; i < mostSignificant0Digits; i++) {
            precedingZeroes = (precedingZeroes | byteHash[i]) & 0x00ff;
        }
        if (height.compareTo(BigInteger.valueOf(32910L)) < 0) {
            if (!(precedingZeroes == 0 && byteHash[mostSignificant0Digits] <= mostSignificantByte)) {
                return false;
            }
        } else {
            if (!(precedingZeroes == 0 && ((byteHash[mostSignificant0Digits] & 0x00ff) <= mostSignificantByte))) {
                return false;
            }
        }
        return true;
    }

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
        ki.debug("Added coinbase");
        for(String key:block.getTransactionKeys())
        {
            if (!ki.getTransMan().addTransactionNoVerify(block.getTransaction(key))) return BlockState.FAILED_ADD_TRANS;
        }
        ki.debug("Added transactions");
        if (!ki.getOptions().nogui) {
            if (block.getCoinbase().getOutputs().get(0).getAddress().encodeForChain().equals(ki.getAddMan().getMainAdd().encodeForChain())) {
                if (ki.getGUIHook() != null) {
                    ki.getGUIHook().blockFound();
                }
            }
            for (String trans : block.getTransactionKeys()) {
                boolean add = false;
                ITrans transaction = block.getTransaction(trans);
                for (IOutput o : transaction.getOutputs()) {
                    for (IAddress a : ki.getAddMan().getAll()) {
                        if (o.getAddress().encodeForChain().equals(a.encodeForChain())) {
                            add = true;
                        }
                    }
                }
                for (IInput i : transaction.getInputs()) {
                    for (IAddress a : ki.getAddMan().getAll()) {
                        if (i.getAddress().encodeForChain().equals(a.encodeForChain())) {
                            add = true;
                        }
                    }
                }
                if (add) {
                    try {
                        ki.getGUIHook().addTransaction(transaction, block.height);
                    } catch (Exception e) {
                        ki.getMainLog().warn("Issue adding transaction to list for GUI.", e);
                    }
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
        try {
            if (blockHeightAmp.get(height.toByteArray()) == null) return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        try {
            return Block.fromAmplet(blockHeightAmp.get(height.toByteArray()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

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
                ki.getMainLog().warn("Re-initializing miners failed after difficulty recalcuation", e);
                return;
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

    @Override
    public synchronized BigInteger getCurrentDifficulty()
    {
        return currentDifficulty;
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
    public synchronized void setDiff(BigInteger diff) {
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
    public Block formEmptyBlock(BigInteger minFee) {
        Block b = new Block();
        b.solver = Utils.toBase64(ki.getAddMan().getMainAdd().toByteArray());
        b.timestamp = System.currentTimeMillis();

        Map<String,ITrans> transactions = new HashMap<>();
        for(ITrans trans:ki.getTransMan().getPending())
        {
            if (trans.getFee().compareTo(minFee) >= 0 && trans.getFee().compareTo(TransactionFeeCalculator.calculateMinFee(trans)) >= 0)
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
        for (Map.Entry<String, ITrans> t : transactions.entrySet())
        {
            fees = fees.add(t.getValue().getFee());
        }
        Output o = new Output(blockRewardForHeight(currentHeight().add(BigInteger.ONE)).add(fees), ki.getAddMan().getMainAdd(), Token.ORIGIN, 0, System.currentTimeMillis(), Output.VERSION);
        List<IOutput> outputs = new ArrayList<>();
        outputs.add(o);
        int i = 1;
        if(b.height.compareTo(BigInteger.ZERO) == 0)
        {
            for (Token t : Token.values())
            {
                if(!t.equals(Token.ORIGIN)) {
                    outputs.add(new Output(BigInteger.valueOf(Long.MAX_VALUE), ki.getAddMan().getMainAdd(), t, i, System.currentTimeMillis(), Output.VERSION));
                    i++;
                }
            }
        }
        ITrans coinbase = null;
        try {
            coinbase = new NewTrans("coinbase", outputs, new ArrayList<>(), new HashMap<>(), TransactionType.NEW_TRANS);
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
        }

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
}