package com.lifeform.main.network.pool;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;

import java.io.Serializable;

public class MiningData implements Serializable, PoolPacket {
    public String data;

    @Override
    public void process(IKi ki, IConnectionManager connMan) {

    }
}
