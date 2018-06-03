package com.ampex.main.network.logic;

import com.ampex.main.IKi;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class Server {

    static final boolean SSL = System.getProperty("ssl") != null;
    private int port;

    private IKi ki;

    public Server(IKi ki,int port)
    {
        this.ki = ki;
        this.port = port;
    }

    ChannelFuture future;
    public void start() throws Exception {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();


            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelInboundHandlerAdapter serverHandler;
                            if (ki.getOptions().poolRelay) {
                                serverHandler = new PoolServerHandler(ki);
                            } else {
                                serverHandler = new ServerHandler(ki);
                            }
                            ChannelPipeline p = ch.pipeline();
                            if (sslCtx != null) {
                                p.addLast(sslCtx.newHandler(ch.alloc()));
                            }
                            p.addLast(
                                    new LengthFieldPrepender(4),
                                    new PacketDecoder(150_000_000, 0, 4, 0, 0),
                                    serverHandler);

                        }
                    }).childOption(ChannelOption.SO_KEEPALIVE,true);

            // Bind and start to accept incoming connections.
            future = b.bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            ki.debug("Connection closed unexpectedly with message: " + e.getMessage());
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void close() {
        future.channel().close();
    }


}
