package com.lifeform.main.network;

import com.lifeform.main.network.packets.PacketGlobal;

public interface IPacketProcessor {


    void process(Object packet);
    PacketGlobal getPacketGlobal();
}
