package com.ampex.main.network.logic;

import com.ampex.main.data.utils.AmpBuildable;
import com.ampex.main.data.utils.AmpBuildableFactory;
import io.netty.channel.ChannelHandlerContext;

public class ServerEndpointHandler implements INetworkEndpoint{

    private ChannelHandlerContext channel;
    @Override
    public void sendPacket(AmpBuildable o) {
        channel.writeAndFlush(AmpBuildableFactory.finalizeBuildAsPacket(o));
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
