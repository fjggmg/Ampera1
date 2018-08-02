package com.ampex.main.network.packets.pool;

import com.ampex.amperabase.AmpBuildable;
import com.ampex.amperabase.IConnectionManager;
import com.ampex.main.IKi;

public interface PoolPacket extends AmpBuildable {
    void process(IKi ki, IConnectionManager connMan);

}
