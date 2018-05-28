package com.ampex.main.network.logic;

import com.ampex.main.data.utils.AmpBuildableFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class PacketDecoder extends LengthFieldBasedFrameDecoder {
    public PacketDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, buf);
        if (frame == null) {
            return null;
        }
        return AmpBuildableFactory.buildPacket(frame.array());
    }
}
