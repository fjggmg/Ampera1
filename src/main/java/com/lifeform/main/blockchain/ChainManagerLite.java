package com.lifeform.main.blockchain;

import com.lifeform.main.IKi;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.data.Utils;
import com.lifeform.main.network.DifficultyRequest;
import com.lifeform.main.transactions.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChainManagerLite implements IChainMan {

    private Block mostRecent;
    private BigInteger currentHeight = BigInteger.valueOf(-1L);
    private BigInteger currentDifficulty = new BigInteger("00000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
    private IKi ki;
    private Block temp;
    private short chainID;
    private Map<BigInteger, Block> chain = new HashMap<>();
    private boolean bDebug = false;

    public ChainManagerLite(IKi ki, short chainID) {
        this.ki = ki;
        this.chainID = chainID;
        bDebug = ki.getOptions().bDebug;
    }

    @Override
    public Map<String, Block> getChain() {
        return null;
    }

    @Override
    public BlockState softVerifyBlock(Block block) {
        Block current = chain.get(block.height.subtract(BigInteger.ONE));
        if (bDebug)
            ki.debug("verifying block...");
        if (block.height.compareTo(currentHeight()) < 0) {
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
        if (new BigInteger(Utils.fromBase64(hash)).abs().compareTo(currentDifficulty) > 0) return BlockState.NO_SOLVE;
        if (bDebug)
            ki.debug("Solves for difficulty");
        if (block.getCoinbase() == null) return BlockState.NO_COINBASE;
        if (bDebug)
            ki.debug("coinbase is in block");
        BigInteger fees = BigInteger.ZERO;

        for (String t : block.getTransactionKeys()) {
            fees = fees.add(block.getTransaction(t).getFee());
        }

        if (!ki.getTransMan().verifyCoinbase(block.getCoinbase(), block.height, fees)) return BlockState.BAD_COINBASE;
        if (bDebug)
            ki.debug("Coinbase verifies ok");
        BlockVerificationHelper bvh = new BlockVerificationHelper(ki, block);
        if (!bvh.verifyTransactions()) return BlockState.BAD_TRANSACTIONS;
        List<String> inputs = new ArrayList<>();
        for (String t : block.getTransactionKeys()) {
            //if(!ki.getTransMan().verifyTransaction(block.getTransaction(t))) return false;
            for (Input i : block.getTransaction(t).getInputs()) {
                if (inputs.contains(i.getID())) return BlockState.DOUBLE_SPEND;
                else inputs.add(i.getID());
            }
        }
        if (bDebug)
            ki.debug("transactions verify ok");
        return BlockState.SUCCESS;
    }

    @Override
    public BlockState verifyBlock(Block b) {
        return null;
    }

    @Override
    public BlockState addBlock(Block b) {
        if (mostRecent == null) {
            mostRecent = b;
            chain.put(b.height, b);
            return BlockState.SUCCESS;
        }
        BlockState bs = softVerifyBlock(b);
        if (bs.success()) {
            if (b.solver.equals(ki.getEncryptMan().getPublicKeyString())) {
                ki.getGUIHook().blockFound();
            }
            for (String trans : b.getTransactionKeys()) {
                boolean add = false;
                ITrans transaction = b.getTransaction(trans);
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
                    ki.getGUIHook().addTransaction(transaction, b.height);
                }
            }
            mostRecent = b;
            chain.put(b.height, b);
            ki.getTransMan().addTransaction(b.getCoinbase());
            currentHeight = b.height;
            for (String trans : b.getTransactionKeys()) {
                ki.getTransMan().addTransaction(b.getTransaction(trans));
            }
            if (chain.keySet().size() > 100) {
                chain.remove(currentHeight.subtract(BigInteger.valueOf(100L)));
            }
            ki.getNetMan().broadcast(new DifficultyRequest());
            return bs;
        }
        return bs;
    }

    @Override
    public void setHeight(BigInteger height) {
        currentHeight = height;
    }

    @Override
    public void loadChain() {

    }

    @Override
    public void saveChain() {

    }

    @Override
    public void close() {

    }

    @Override
    public void clearFile() {

    }

    @Override
    public void saveBlock(Block b) {

    }

    @Override
    public BigInteger currentHeight() {
        return currentHeight;
    }

    @Override
    public Block getByHeight(BigInteger height) {
        return chain.get(height);
    }

    @Override
    public BigInteger getCurrentDifficulty() {
        return currentDifficulty;
    }

    @Override
    public Block getByID(String ID) {
        return null;
    }

    @Override
    public void undoToBlock(String ID) {

    }

    @Override
    public boolean canMine() {
        return true;
    }

    @Override
    public void setCanMine(boolean canMine) {

    }

    @Override
    public void verifyLater(Block b) {

    }

    public void setDifficulty(BigInteger difficulty) {
        currentDifficulty = difficulty;
    }
    @Override
    public Block formEmptyBlock() {
        Block b = new Block();
        b.solver = ki.getEncryptMan().getPublicKeyString();
        b.timestamp = System.currentTimeMillis();

        Map<String, ITrans> transactions = new HashMap<>();
        for (ITrans trans : ki.getTransMan().getPending()) {
            transactions.put(trans.getID(), trans);
        }
        b.addAll(transactions);

        b.height = currentHeight().add(BigInteger.ONE);
        //if(b.height.compareTo(BigInteger.ZERO) == 0) b.height = b.height.add(BigInteger.ONE);
        if (!(currentHeight().compareTo(BigInteger.valueOf(-1L)) == 0)) {
            if (mostRecent == null) ki.getMainLog().info("Current is null");
            b.prevID = mostRecent.ID;
        } else
            b.prevID = "0";

        BigInteger fees = BigInteger.ZERO;
        for (String t : transactions.keySet()) {
            fees = fees.add(transactions.get(t).getFee());
        }
        Output o = new Output(ChainManager.blockRewardForHeight(currentHeight().add(BigInteger.ONE)).add(fees), ki.getAddMan().getMainAdd(), Token.ORIGIN, 0, System.currentTimeMillis());
        List<Output> outputs = new ArrayList<>();
        outputs.add(o);
        int i = 1;
        if (b.height.compareTo(BigInteger.ZERO) == 0) {
            for (Token t : Token.values()) {
                if (!t.equals(Token.ORIGIN)) {
                    outputs.add(new Output(BigInteger.valueOf(Long.MAX_VALUE), ki.getAddMan().getMainAdd(), t, i, System.currentTimeMillis()));
                    i++;
                }
            }
        }
        ITrans coinbase = new Transaction("coinbase", 0, new HashMap<String, String>(), outputs, new ArrayList<Input>(), new HashMap<>(), new ArrayList<String>());

        b.setCoinbase(coinbase);
        //b.addTransaction(coinbase);
        b.ID = EncryptionManager.sha512(b.header());

        return b;
    }

    @Override
    public BigInteger calculateDiff(BigInteger currentDiff, long timeElapsed) {
        return null;
    }

    @Override
    public short getChainVer() {
        return chainID;
    }

    @Override
    public Block getTemp() {
        return temp;
    }

    @Override
    public void setTemp(Block b) {
        this.temp = temp;
    }
}
