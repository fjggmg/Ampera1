package com.lifeform.main.network.logic;

import io.netty.channel.Channel;

public class ServerEndpointHandler implements INetworkEndpoint{

    private Channel channel;
    @Override
    public void sendPacket(Object o) {
        channel.writeAndFlush(o);
    }

    @Override
    public void setChannel(Channel c) {
        channel = c;
    }
}
