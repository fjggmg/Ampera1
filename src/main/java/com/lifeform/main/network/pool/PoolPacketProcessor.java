package com.lifeform.main.network.pool;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;

public class PoolPacketProcessor {

    private IKi ki;

    public PoolPacketProcessor(IKi ki) {
        this.ki = ki;
    }

    public void process(Object o, IConnectionManager connMan) {
        if (!(o instanceof PoolPacket)) return;
        ((PoolPacket) o).process(ki, connMan);

    }


}
