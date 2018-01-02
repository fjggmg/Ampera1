package com.lifeform.main.network.logic;

import com.lifeform.main.IKi;
import com.lifeform.main.network.ConnMan;
import com.lifeform.main.network.IConnectionManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private IKi ki;

    public ServerHandler(IKi ki)
    {
        this.ki = ki;
    }
    private IConnectionManager connMan;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // Echo back the received object to the client.
        try {
            //if(ki.getOptions().pDebug)
            //ki.debug("Received packet: " + msg.toString());

            connMan.received(msg);
        }finally{
            ReferenceCountUtil.release(msg);
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        INetworkEndpoint endpoint = new ServerEndpointHandler();
        endpoint.setChannel(ctx.channel());
        connMan = new ConnMan(ki,true,endpoint);
        //ki.getNetMan().getConnections().add(connMan);
        connMan.connected();
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