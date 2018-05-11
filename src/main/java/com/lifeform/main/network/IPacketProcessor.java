package com.lifeform.main.network;

import com.lifeform.main.network.packets.PacketGlobal;

public interface IPacketProcessor {

    /**
     * WARNING! THIS WILL IMMEDIATELY PROCESS A PACKET AND SHOULD NOT BE USED WITH A MULTI-PACKET OBJECT IN WHICH ORDER MUST BE RETAINED
     * @param packet packet to process
     */
    void process(Object packet);
    void enqueue(Object packet);
    PacketGlobal getPacketGlobal();
    //Thread getThread();
}
