package com.ampex.main.network.packets.pool;

import com.ampex.main.IKi;
import com.ampex.main.data.utils.AmpBuildable;
import com.ampex.main.network.IConnectionManager;

public interface PoolPacket extends AmpBuildable {
    void process(IKi ki, IConnectionManager connMan);

}
