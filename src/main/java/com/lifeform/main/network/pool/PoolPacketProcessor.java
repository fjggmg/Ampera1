package com.lifeform.main.network.pool;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;
import com.lifeform.main.network.IPacketProcessor;
import com.lifeform.main.network.PacketGlobal;

public class PoolPacketProcessor implements IPacketProcessor {

    private IConnectionManager connMan;

    public PoolPacketProcessor(IKi ki, IConnectionManager connMan) {
        this.connMan = connMan;
        this.ki = ki;
    }
    private IKi ki;


    @Override
    public void process(Object o) {
        if (!(o instanceof PoolPacket)) return;
        ((PoolPacket) o).process(ki, connMan);
    }

    @Override
    public void enqueue(Object packet) {

    }

    @Override
    public PacketGlobal getPacketGlobal() {
        return null;
    }
}
