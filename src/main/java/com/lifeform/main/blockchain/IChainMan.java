package com.lifeform.main.blockchain;

import java.math.BigInteger;
import java.util.Map;

/**
 * Created by Bryan on 7/14/2017.
 */
public interface IChainMan {

    Map<String,Block> getChain();

    BlockState softVerifyBlock(Block b);

    BlockState verifyBlock(Block b);

    BlockState addBlock(Block b);

    void setHeight(BigInteger height);
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

    short getChainVer();

}
