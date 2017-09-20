package com.lifeform.main.network.logic;

import io.netty.channel.Channel;

public interface INetworkEndpoint {
    void sendPacket(Object o);
    void setChannel(Channel c);
}
