package com.lifeform.main.blockchain;

import com.lifeform.main.IKi;
import com.lifeform.main.data.EncryptionManager;
import com.lifeform.main.transactions.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChainManagerLite implements IChainMan {

    private Block mostRecent;
    private BigInteger currentHeight;
    private BigInteger currentDifficulty;
    private IKi ki;
    private Block temp;
    private short chainID;

    public ChainManagerLite(IKi ki) {
        this.ki = ki;
    }

    @Override
    public Map<String, Block> getChain() {
        return null;
    }

    @Override
    public BlockState softVerifyBlock(Block b) {
        return null;
    }

    @Override
    public BlockState verifyBlock(Block b) {
        return null;
    }

    @Override
    public BlockState addBlock(Block b) {
        return null;
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
        return null;
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
            if (getByHeight(currentHeight()) == null) ki.getMainLog().info("Current is null");
            b.prevID = getByHeight(currentHeight()).ID;
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
