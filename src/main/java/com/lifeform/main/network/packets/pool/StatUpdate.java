package com.lifeform.main.network.packets.pool;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;

import java.io.Serializable;

public class StatUpdate implements Serializable, PoolPacket {
    private static final long serialVersionUID = 184L;
    public long shares;
    public double currentPPS;

    @Override
    public void process(IKi ki, IConnectionManager connMan) {
        ki.debug("===============Received stat update=======================");
        ki.debug("shares: " + shares);
        ki.debug("pps: " + currentPPS);
        ki.getGUIHook().updatePoolStats(shares, currentPPS);
    }
}
