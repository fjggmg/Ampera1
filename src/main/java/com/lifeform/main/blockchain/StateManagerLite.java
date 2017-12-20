package com.lifeform.main.blockchain;

import com.lifeform.main.IKi;

public class StateManagerLite implements IStateManager {
    private IKi ki;

    public StateManagerLite(IKi ki) {
        this.ki = ki;
    }

    @Override
    public void addBlock(Block block, String connID) {
        if (!ki.getChainMan().addBlock(block).success()) {
            ki.resetLite();
        }
    }

    @Override
    public void start() {

    }
}
