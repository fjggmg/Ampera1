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
        //System.out.println("=========================Received bytes from network, working on decode");
        ByteBuf frame = (ByteBuf) super.decode(ctx, buf);
        if (frame == null) {
            return null;
        }
        int size = frame.getInt(0);
        frame.skipBytes(4);
        byte[] data = new byte[size];
        frame.readBytes(data);
        frame.release();
        //buf.release();
        //ReferenceCountUtil.release(frame);
        //System.out.println("Frame not null, length: " + size);
        return AmpBuildableFactory.buildPacket(data);
    }
}
