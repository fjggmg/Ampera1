package com.lifeform.main.network.pool;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;

import java.io.Serializable;
import java.math.BigInteger;

public class UpdatePPS implements Serializable, PoolPacket {
    BigInteger newPPS;

    @Override
    public void process(IKi ki, IConnectionManager connMan) {
        //TODO update for GUI reasons on client side
    }
}
