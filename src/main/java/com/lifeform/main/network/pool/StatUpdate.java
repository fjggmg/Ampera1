package com.lifeform.main.network.pool;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;


import java.io.Serializable;

public class StatUpdate implements Serializable, PoolPacket {
    public long shares;
    public double currentPPS;

    @Override
    public void process(IKi ki, IConnectionManager connMan) {
        ki.getGUIHook().updatePoolStats(shares, currentPPS);
    }
}
