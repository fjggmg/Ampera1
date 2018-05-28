package com.ampex.main.blockchain;

import com.ampex.amperabase.*;
import com.ampex.main.IKi;
import com.ampex.main.data.encryption.EncryptionManager;
import com.ampex.main.data.utils.Utils;
import com.ampex.main.network.packets.DifficultyRequest;
import com.ampex.main.transactions.*;

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

        if (block.timestamp > System.currentTimeMillis() + 60000L) return BlockState.TIMESTAMP_WRONG;
        if (bDebug)
            ki.debug("timestamp is OK");
        String hash = EncryptionManager.sha512(block.header());
        if (!block.ID.equals(hash)) return BlockState.ID_MISMATCH;
        if (bDebug)
            ki.debug("ID is ok");
        if (new BigInteger(Utils.fromBase64(hash)).abs().compareTo(currentDifficulty) > 0)
            return BlockState.NO_SOLVE;
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
            for (IInput i : block.getTransaction(t).getInputs()) {
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
        if (mostRecent == null || (b.height.compareTo(mostRecent.height) > 0)) {
            mostRecent = b;
            chain.put(b.height, b);

            if (b.getCoinbase().getOutputs().get(0).getAddress().encodeForChain().equals(ki.getAddMan().getMainAdd().encodeForChain())) {
                if (ki.getGUIHook() != null)
                    ki.getGUIHook().blockFound();
            }
            for (String trans : b.getTransactionKeys()) {
                boolean add = false;
                ITrans transaction = b.getTransaction(trans);
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
                    if (ki.getGUIHook() != null)
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
            if (ki.getOptions().poolRelay) {
                ki.getPoolManager().updateCurrentHeight(ki.getChainMan().currentHeight());
            }
            if (ki.getMinerMan() != null && ki.getMinerMan().isMining()) {
                ki.debug("Restarting miners");
            /* old miner stuff
             CPUMiner.height = ki.getChainMan().currentHeight().add(BigInteger.ONE);
             CPUMiner.prevID = ki.getChainMan().getByHeight(ki.getChainMan().currentHeight()).ID;
             */

                ki.getMinerMan().restartMiners();
            }
            return BlockState.SUCCESS;
        }
        return BlockState.WRONG_HEIGHT;
    }

    @Override
    public void setHeight(BigInteger height) {
        currentHeight = height;
    }

    @Override
    public void loadChain() {

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
    public boolean canMine() {
        return true;
    }

    @Override
    public void setCanMine(boolean canMine) {

    }

    public void setDifficulty(BigInteger difficulty) {
        currentDifficulty = difficulty;
    }
    @Override
    public Block formEmptyBlock(BigInteger minFee) {
        Block b = new Block();
        b.solver = Utils.toBase64(ki.getAddMan().getMainAdd().toByteArray());
        b.timestamp = System.currentTimeMillis();

        Map<String, ITrans> transactions = new HashMap<>();
        for (ITrans trans : ki.getTransMan().getPending()) {
            if (trans.getFee().compareTo(minFee) >= 0 && trans.getFee().compareTo(TransactionFeeCalculator.calculateMinFee(trans)) >= 0)
                transactions.put(trans.getID(), trans);
        }
        b.addAll(transactions);

        b.height = currentHeight().add(BigInteger.ONE);
        //if(b.height.compareTo(BigInteger.ZERO) == 0) b.height = b.height.add(BigInteger.ONE);
        if (!(currentHeight().compareTo(BigInteger.valueOf(-1L)) == 0)) {
            if (mostRecent == null) {
                ki.getMainLog().info("Current is null");
            } else {
                b.prevID = mostRecent.ID;
            }
        } else
            b.prevID = "0";

        BigInteger fees = BigInteger.ZERO;
        for (Map.Entry<String, ITrans> t : transactions.entrySet()) {
            fees = fees.add(t.getValue().getFee());
        }
        Output o = new Output(ChainManager.blockRewardForHeight(currentHeight().add(BigInteger.ONE)).add(fees), ki.getAddMan().getMainAdd(), Token.ORIGIN, 0, System.currentTimeMillis(), Output.VERSION);
        List<IOutput> outputs = new ArrayList<>();
        outputs.add(o);
        int i = 1;
        if (b.height.compareTo(BigInteger.ZERO) == 0) {
            for (Token t : Token.values()) {
                if (!t.equals(Token.ORIGIN)) {
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

    @Override
    public void startCache(BigInteger height) {

    }

    @Override
    public void stopCache() {

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
        this.temp = b;
    }

    @Override
    public void undoToBlock(BigInteger height) {

    }

    @Override
    public void setDiff(BigInteger diff) {

    }
}
