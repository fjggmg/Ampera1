package com.ampex.main.network.logic;

import com.ampex.amperabase.IConnectionManager;
import com.ampex.main.IKi;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    private IKi ki;
    private IConnectionManager connMan;
    private Client client;

    public ClientHandler(IKi ki, IConnectionManager connMan,Client client) {
        this.ki = ki;
        this.connMan = connMan;
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // Send the first message if this handler is a client-side handler.
        client.setChannel(ctx);
        connMan.connected();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            //if(ki.getOptions().pDebug)
            //ki.debug("Received packet: " + msg.toString());
            connMan.received(msg);
        }finally{
            ReferenceCountUtil.release(msg);
        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ki.debug("Error caught on client connection: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
