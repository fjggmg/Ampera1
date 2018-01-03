package com.lifeform.main.network;

public interface IPacketProcessor {

    /**
     * WARNING! THIS WILL IMMEDIATELY PROCESS A PACKET AND SHOULD NOT BE USED WITH A MULTI-PACKET OBJECT IN WHICH ORDER MUST BE RETAINED
     * @param packet
     */
    void process(Object packet);
    void enqueue(Object packet);
    PacketGlobal getPacketGlobal();
    Thread getThread();
}
