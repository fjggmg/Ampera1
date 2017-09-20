package com.lifeform.main.network.logic;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    private IKi ki;
    private IConnectionManager connMan;
    private Client client;
    /**
     * Creates a client-side handler.
     */
    public ClientHandler(IKi ki, IConnectionManager connMan,Client client) {
        this.ki = ki;
        this.connMan = connMan;
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // Send the first message if this handler is a client-side handler.
        connMan.connected();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            ki.debug("Received packet: " + msg.toString());

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
        cause.printStackTrace();
        ctx.close();
    }
}
