package com.ampex.main.network;

import io.netty.channel.Channel;

public interface ChannelHandler {
    Channel getChannel();
}
