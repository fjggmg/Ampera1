package com.lifeform.main.network.pool;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;

public interface PoolPacket {
    void process(IKi ki, IConnectionManager connMan);

}
