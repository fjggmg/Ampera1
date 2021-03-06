package com.ampex.main.network.logic;

import com.ampex.amperabase.IConnectionManager;
import com.ampex.main.IKi;
import com.ampex.main.network.pool.PoolConnMan;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class PoolServerHandler extends ChannelInboundHandlerAdapter {
    private IKi ki;

    PoolServerHandler(IKi ki) {
        this.ki = ki;
    }

    private IConnectionManager connMan;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // Echo back the received object to the client.
        try {
            //if(ki.getOptions().pDebug)
            //ki.debug("Received packet: " + msg.toString());
            if (connMan != null)
            connMan.received(msg);
        } finally {
            ReferenceCountUtil.release(msg);
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        INetworkEndpoint endpoint = new ServerEndpointHandler();
        endpoint.setChannel(ctx);
        connMan = new PoolConnMan(ki, endpoint);
        //ki.getNetMan().getConnections().add(connMan);
        connMan.connected();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //cause.printStackTrace();
        ki.debug("Error caught on server connection: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
