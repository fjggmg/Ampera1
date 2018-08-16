package com.ampex.main.network.rest.pool;

import com.ampex.amperabase.IConnectionManager;
import com.ampex.main.IKi;
import com.ampex.main.network.logic.INetworkEndpoint;
import com.ampex.main.network.logic.ServerEndpointHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class PoolRestServerHandler extends ChannelInboundHandlerAdapter {
    private IKi ki;

    public PoolRestServerHandler(IKi ki)
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
            if (connMan != null)
                connMan.received(msg);
        }finally{
            ReferenceCountUtil.release(msg);
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        INetworkEndpoint endpoint = new ServerEndpointHandler();
        endpoint.setChannel(ctx);
        connMan = new PoolRestConnMan(ki,endpoint);
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
        ki.getMainLog().error("Error caught on server connection: ", cause);
        ctx.close();
    }
}
