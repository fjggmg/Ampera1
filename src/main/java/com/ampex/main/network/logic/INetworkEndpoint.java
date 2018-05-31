package com.ampex.main.network.logic;

import com.ampex.amperabase.AmpBuildable;
import io.netty.channel.ChannelHandlerContext;

public interface INetworkEndpoint {
    void sendPacket(AmpBuildable o);

    void setChannel(ChannelHandlerContext c);

    boolean isConnected();
    String getAddress();
    void disconnect();

    ChannelHandlerContext getChannel();
}
