package com.ampex.main.blockchain;

import com.ampex.amperabase.IChainManAPI;

import java.math.BigInteger;

/**
 * Created by Bryan on 7/14/2017.
 */
public interface IChainMan extends IChainManAPI {

    void setHeight(BigInteger height);
    void loadChain();

    void close();

    void clearFile();

    BigInteger currentHeight();

    BigInteger getCurrentDifficulty();

    boolean canMine();

    void setCanMine(boolean canMine);

    Block formEmptyBlock(BigInteger minFee);

    void startCache(BigInteger height);

    void stopCache();

    BigInteger calculateDiff(BigInteger currentDiff,long timeElapsed);

    short getChainVer();

    void setTemp(Block b);

    void undoToBlock(BigInteger height);

    void setDiff(BigInteger diff);

}
