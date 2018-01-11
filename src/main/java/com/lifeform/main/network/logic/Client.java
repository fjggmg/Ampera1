package com.lifeform.main.network.logic;

import com.lifeform.main.IKi;
import com.lifeform.main.network.IConnectionManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Client implements INetworkEndpoint{

    static final boolean SSL = System.getProperty("ssl") != null;
    private String host;
    private int port;
    static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));
    private IKi ki;
    Client instance;
    public Client(IKi ki,String host,int port)
    {
        this.ki = ki;
        this.host = host;
        this.port = port;
        instance = this;
    }

    public void setChannel(ChannelHandlerContext c)
    {
        this.channel = c;
    }

    @Override
    public boolean isConnected() {
        return channel.channel().isActive();
    }

    @Override
    public String getAddress() {

        if (channel == null) return "Channel Null";
        if (channel.channel().remoteAddress() == null) return "Address Null";
        return channel.channel().remoteAddress().toString();
    }

    @Override
    public void disconnect() {
        if (channel != null)
            channel.disconnect();
    }

    @Override
    public ChannelHandlerContext getChannel() {
        return channel;
    }

    private ChannelHandlerContext channel;
    public void start(IConnectionManager connMan) throws Exception {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            sslCtx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {

                            ChannelPipeline p = ch.pipeline();
                            if (sslCtx != null) {
                                p.addLast(sslCtx.newHandler(ch.alloc(), host,port));
                            }
                            p.addLast(
                                    new ObjectEncoder(),
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new ClientHandler(ki,connMan,instance));
                            //ch.write("This is a test 2");

                        }
                    });

            // Start the connection attempt.
            b.connect(host, port).channel().closeFuture().sync();
        } catch (Exception e) {
            ki.debug("Connection closed unexpectedly with message: " + e.getMessage());
            //ki.restartNetwork();
        } finally {
            group.shutdownGracefully();
            connMan.disconnect();
        }
    }
    public void sendPacket(Object o)
    {
        channel.writeAndFlush(o);
    }
}
