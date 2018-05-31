package com.ampex.main.blockchain;

import amp.Amplet;
import com.ampex.amperabase.BlockState;
import com.ampex.amperabase.IBlockAPI;
import com.ampex.amperabase.InvalidTransactionException;
import com.ampex.main.Ki;

import java.math.BigInteger;

public class PoolChainMan implements IChainMan {
    private IBlockAPI toMine;

    @Override
    public BlockState softVerifyBlock(IBlockAPI b) {
        return null;
    }

    @Override
    public BlockState verifyBlock(IBlockAPI b) {
        return null;
    }

    @Override
    public void setDifficulty(BigInteger bigInteger) {

    }

    @Override
    public BlockState addBlock(IBlockAPI b) {
        toMine = b;
        return BlockState.SUCCESS;
    }

    @Override
    public void setHeight(BigInteger height) {

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
    public BigInteger currentHeight() {
        return null;
    }

    @Override
    public Block getByHeight(BigInteger height) {
        return null;
    }

    @Override
    public BigInteger getCurrentDifficulty() {
        return null;
    }

    @Override
    public boolean canMine() {
        return true;
    }

    @Override
    public void setCanMine(boolean canMine) {

    }

    //TODO casting to implementation here, check this later
    @Override
    public Block formEmptyBlock(BigInteger minFee) {
        return (Block) toMine;
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
        return 0;
    }

    @Override
    public Block getTemp() {
        return null;
    }

    @Override
    public IBlockAPI formBlock(BigInteger height, String ID, String merkleRoot, byte[] payload, String prevID, String solver, long timestamp, byte[] coinbase) {
        Block block = new Block();
        block.height = height;
        block.ID = ID;
        block.merkleRoot = merkleRoot;
        block.payload = payload;
        block.prevID = prevID;
        block.solver = solver;
        block.timestamp = timestamp;
        try {
            block.setCoinbase(Ki.getInstance().getTransMan().deserializeTransaction(Amplet.create(coinbase)));
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
        }
        return block;
    }

    @Override
    public IBlockAPI formBlock(Amplet amplet) {
        return Block.fromAmplet(amplet);
    }

    @Override
    public void setTemp(Block b) {

    }

    @Override
    public void undoToBlock(BigInteger height) {

    }

    @Override
    public void setDiff(BigInteger diff) {

    }
}
