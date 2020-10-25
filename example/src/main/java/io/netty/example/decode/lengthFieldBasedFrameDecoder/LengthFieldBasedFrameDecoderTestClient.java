package io.netty.example.decode.lengthFieldBasedFrameDecoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author: wenyixicodedog
 * @create: 2020-10-18
 * @description: 定长解码器客户端
 */
public class LengthFieldBasedFrameDecoderTestClient {

    public static void main(String[] args) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new ChannelInboundHandlerAdapter() {
                                        public void channelActive(ChannelHandlerContext ctx) {
                                            ByteBuf byteBuf = Unpooled.copiedBuffer("hello world".getBytes());
                                            ctx.writeAndFlush(byteBuf);
                                        }
                                    });
                        }
                    });
            ChannelFuture f = b.connect("127.0.0.1", 9000).sync();
            System.out.println("Started DelimiterBasedFrameDecoderTestClient...");
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

}
