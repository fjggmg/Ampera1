package com.lifeform.main.blockchain;

import gpuminer.miner.context.ContextMaster;

import java.util.List;

public interface IMinerMan {

    void startMiners();
    void stopMiners();

    void restartMiners();
    List<IMiner> getMiners();
    boolean isMining();

    boolean miningCompatible();

    List<String> getDevNames();

    void enableDev(String dev);

    void disableDev(String dev);

    long cumulativeHashrate();

    void setHashrate(String dev, long rate);

    void setup();

    boolean isSetup();

    ContextMaster getContextMaster();
}
