package com.lifeform.main.blockchain;

import java.math.BigInteger;
import java.util.Map;

public class PoolChainMan implements IChainMan {
    private Block toMine;

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
    public Block formEmptyBlock(BigInteger minFee) {
        return toMine;
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
    public void setTemp(Block b) {

    }

    @Override
    public void undoToBlock(BigInteger height) {

    }

    @Override
    public void setDiff(BigInteger diff) {

    }
}
