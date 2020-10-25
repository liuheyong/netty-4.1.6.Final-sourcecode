package io.netty.example.decode.delimiterBasedFrameDecoder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

import java.nio.charset.Charset;

/**
 * @author: wenyixicodedog
 * @create: 2020-10-18
 * @description: 分隔符解码器服务端
 */
public class DelimiterBasedFrameDecoderTestServer {

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ByteBuf delimiter1 = Unpooled.copiedBuffer("%".getBytes());
                            ByteBuf delimiter2 = Unpooled.copiedBuffer("$".getBytes());
                            ch.pipeline()
                                    .addLast(new DelimiterBasedFrameDecoder(1024, delimiter1, delimiter2))
                                    .addLast(new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                            if (msg instanceof ByteBuf) {
                                                ByteBuf packet = (ByteBuf) msg;
                                                System.out.println(packet.toString(Charset.defaultCharset()));
                                            }
                                        }
                                    });
                        }
                    });
            ChannelFuture f = b.bind(9000).sync();
            System.out.println("Started DelimiterBasedFrameDecoderTestServer...");
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
