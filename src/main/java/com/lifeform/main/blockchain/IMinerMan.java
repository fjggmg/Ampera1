package com.lifeform.main.blockchain;

import java.util.List;

public interface IMinerMan {

    void startMiners(double count);
    void startMiners(int count);
    void stopMiners();
    void restartMiners(int count);
    List<IMiner> getMiners();
    boolean isMining();
    int getPreviousCount();
}
