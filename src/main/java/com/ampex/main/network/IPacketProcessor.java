package com.ampex.main.network;

import com.ampex.main.network.packets.PacketGlobal;

public interface IPacketProcessor {


    void process(Object packet);
    PacketGlobal getPacketGlobal();
}
