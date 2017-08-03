package com.lifeform.main.blockchain;

import java.math.BigInteger;
import java.util.Map;

/**
 * Created by Bryan on 7/14/2017.
 */
public interface IChainMan {

    Map<String,Block> getChain();

    boolean softVerifyBlock(Block b);
    boolean verifyBlock(Block b);

    boolean addBlock(Block b);

    void loadChain();

    @Deprecated
    void saveChain();

    void close();

    void clearFile();

    void saveBlock(Block b);

    BigInteger currentHeight();

    Block getByHeight(BigInteger height);

    BigInteger getCurrentDifficulty();

    Block getByID(String ID);

    void undoToBlock(String ID);

    boolean canMine();

    void setCanMine(boolean canMine);

    void verifyLater(Block b);

    Block formEmptyBlock();

    BigInteger calculateDiff(BigInteger currentDiff,long timeElapsed);

}
