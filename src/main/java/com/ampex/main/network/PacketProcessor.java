package com.ampex.main.network;

import com.ampex.main.IKi;
import com.ampex.main.network.packets.Packet;
import com.ampex.main.network.packets.PacketGlobal;

public class PacketProcessor implements IPacketProcessor{

    private IKi ki;

    public PacketProcessor(IKi ki,IConnectionManager connMan)
    {
        this.connMan = connMan;
        this.ki = ki;
        pg = new PacketGlobal(ki, connMan);
    }

    private IConnectionManager connMan;
    private PacketGlobal pg;
    @Override
    public void process(Object packet) {
        //typecheck keeps us sane, and is relatively cheap
        if (packet instanceof Packet)
        {
            ((Packet) packet).process(ki, connMan, pg);
        } else {
            if(ki.getOptions().pDebug)
            ki.debug("Received unknown packet from " + connMan.getAddress());
        }
    }

    @Override
    public PacketGlobal getPacketGlobal() {
        return pg;
    }
}