package com.ampex.main.network.packets.pool;

import amp.HeadlessAmplet;
import com.ampex.amperabase.IConnectionManager;
import com.ampex.amperabase.InvalidAmpBuildException;
import com.ampex.main.IKi;

public class StatUpdate implements PoolPacket {

    public long shares;
    public double currentPPS;

    @Override
    public void process(IKi ki, IConnectionManager connMan) {
        ki.debug("===============Received stat update=======================");
        ki.debug("shares: " + shares);
        ki.debug("pps: " + currentPPS);
        ki.getGUIHook().updatePoolStats(shares, currentPPS);
    }

    @Override
    public void build(byte[] serialized) throws InvalidAmpBuildException {
        try {
            HeadlessAmplet ha = HeadlessAmplet.create(serialized);
            shares = ha.getNextLong();
            currentPPS = ha.getNextDouble();
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new InvalidAmpBuildException("Unable to create StatUpdate from bytes");
        }
    }

    @Override
    public byte[] serializeToBytes() {
        HeadlessAmplet ha = HeadlessAmplet.create();
        ha.addElement(shares);
        ha.addElement(currentPPS);
        return ha.serializeToBytes();
    }
}
