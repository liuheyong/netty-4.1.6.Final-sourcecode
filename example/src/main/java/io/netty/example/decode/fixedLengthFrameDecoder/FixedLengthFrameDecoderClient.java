package io.netty.example.decode.fixedLengthFrameDecoder;

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
public class FixedLengthFrameDecoderClient {

    public static void main(String[] args) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            //在与server建立连接后，即发送请求报文
                            public void channelActive(ChannelHandlerContext ctx) {
                                ByteBuf A = Unpooled.buffer().writeBytes("A".getBytes());
                                ByteBuf BC = Unpooled.buffer().writeBytes("BC".getBytes());
                                ByteBuf DEFG = Unpooled.buffer().writeBytes("DEFG".getBytes());
                                ByteBuf HI = Unpooled.buffer().writeBytes("HI".getBytes());
                                ctx.writeAndFlush(A);
                                ctx.writeAndFlush(BC);
                                ctx.writeAndFlush(DEFG);
                                ctx.writeAndFlush(HI);
                            }
                        });
                    }
                });
            ChannelFuture f = b.connect("127.0.0.1", 8080).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

}
