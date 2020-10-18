package io.netty.example.decode.fixedLengthFrameDecoder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;

import java.nio.charset.Charset;

/**
 * @author: wenyixicodedog
 * @create: 2020-10-18
 * @description: 定长解码器服务端
 */
public class FixedLengthFrameDecoderTestServer {

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new FixedLengthFrameDecoder(4))
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
            System.out.println("Started FixedLengthFrameDecoderTestServer...");
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
