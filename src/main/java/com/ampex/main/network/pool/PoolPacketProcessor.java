package com.ampex.main.network.pool;

import com.ampex.amperabase.IConnectionManager;
import com.ampex.amperabase.IPacketProcessor;
import com.ampex.amperanet.packets.PacketGlobal;
import com.ampex.main.IKi;
import com.ampex.main.network.packets.pool.PoolPacket;

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
    public PacketGlobal getPacketGlobal() {
        return null;
    }
}
