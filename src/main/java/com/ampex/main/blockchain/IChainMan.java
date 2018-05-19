package com.ampex.main.blockchain;

import com.ampex.amperabase.IChainManAPI;

import java.math.BigInteger;

/**
 * Created by Bryan on 7/14/2017.
 */
public interface IChainMan extends IChainManAPI {
    int MAX_TXIOS = 100_000;

    BlockState softVerifyBlock(Block b);

    BlockState verifyBlock(Block b);

    BlockState addBlock(Block b);

    void setHeight(BigInteger height);
    void loadChain();

    void close();

    void clearFile();

    void saveBlock(Block b);

    BigInteger currentHeight();

    Block getByHeight(BigInteger height);

    BigInteger getCurrentDifficulty();

    boolean canMine();

    void setCanMine(boolean canMine);

    Block formEmptyBlock(BigInteger minFee);

    void startCache(BigInteger height);

    void stopCache();

    BigInteger calculateDiff(BigInteger currentDiff,long timeElapsed);

    short getChainVer();

    Block getTemp();

    void setTemp(Block b);

    void undoToBlock(BigInteger height);

    void setDiff(BigInteger diff);

}
