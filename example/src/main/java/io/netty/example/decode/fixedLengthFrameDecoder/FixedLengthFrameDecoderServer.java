package io.netty.example.decode.fixedLengthFrameDecoder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;

import java.nio.charset.Charset;
import java.util.Date;

/**
 * @author: wenyixicodedog
 * @create: 2020-10-18
 * @description: 定长解码器服务端
 */
public class FixedLengthFrameDecoderServer {

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new FixedLengthFrameDecoder(3));
                        // 自定义这个ChannelInboundHandler打印拆包后的结果
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                if (msg instanceof ByteBuf) {
                                    ByteBuf packet = (ByteBuf) msg;
                                    System.out.println(new Date().toLocaleString() + ":" + packet.toString(Charset.defaultCharset()));
                                }
                            }
                        });
                    }
                });
            ChannelFuture f = b.bind(8080).sync();
            System.out.println("FixedLengthFrameDecoderServer Started on 8080...");
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}
