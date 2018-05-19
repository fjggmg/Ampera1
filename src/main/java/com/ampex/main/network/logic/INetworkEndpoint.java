package com.ampex.main.network.logic;

import io.netty.channel.ChannelHandlerContext;

public interface INetworkEndpoint {
    void sendPacket(Object o);

    void setChannel(ChannelHandlerContext c);

    boolean isConnected();
    String getAddress();
    void disconnect();

    ChannelHandlerContext getChannel();
}
