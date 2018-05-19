package com.ampex.main.network.logic;

import io.netty.channel.ChannelHandlerContext;

public class ServerEndpointHandler implements INetworkEndpoint{

    private ChannelHandlerContext channel;
    @Override
    public void sendPacket(Object o) {
        channel.writeAndFlush(o);
    }

    @Override
    public void setChannel(ChannelHandlerContext c) {
        channel = c;
    }

    @Override
    public boolean isConnected() {
        return channel.channel().isActive();
    }

    @Override
    public String getAddress() {
        return channel.channel().remoteAddress().toString();
    }

    @Override
    public void disconnect() {
        channel.disconnect();
    }

    @Override
    public ChannelHandlerContext getChannel() {
        return channel;
    }
}
