package com.ampex.main.blockchain;

public interface IStateManager {

    void addBlock(Block block, String connID);

    void start();

    void interrupt();

}
