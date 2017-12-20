package com.lifeform.main.blockchain;

public interface IStateManager {

    void addBlock(Block block, String connID);

    void start();

}
