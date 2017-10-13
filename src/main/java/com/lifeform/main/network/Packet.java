package com.lifeform.main.network;

import com.lifeform.main.IKi;

public interface Packet {

    /**
     * Generic process method, takes the god object to give access to whatever subsystem this may need, As much as I'd like
     * to specialize this, abstracting it makes it easier to deal with and will probably not suffer a performance hit
     *
     * @param ki      god object
     * @param connMan connection manager this packet is handled by -- this gives us freedom with processing because we can be sure where we belong
     * @param pg      this is a state handler, helps us keep track of current state of the node
     */
    void process(IKi ki, IConnectionManager connMan, PacketGlobal pg);

    /**
     * this is a magic number but it helps save space when sending packet types and gives us
     * plenty of room for packets in the future, the biggest issue here is keeping types clear
     *
     * @return the int corresponding to the packet type
     */
    int packetType();
}
