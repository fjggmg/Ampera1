package com.ampex.main.network.packets.pool;

import com.ampex.main.IKi;
import com.ampex.main.network.IConnectionManager;

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
