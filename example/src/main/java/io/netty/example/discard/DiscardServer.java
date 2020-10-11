/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.example.discard;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * Discards any incoming data.
 */
public final class DiscardServer {

    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", "8009"));

    public static void main(String[] args) throws Exception {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        //NioEventLoopGroup循环，不停地检测IO事件，处理IO事件，执行任务
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //ServerBootstrap 是服务端的一个启动辅助类，通过给他设置一系列参数来绑定端口启动服务
            ServerBootstrap b = new ServerBootstrap();
            //bossGroup的作用就是不断地accept到新的连接，将新的连接丢给workerGroup来处理
            b.group(bossGroup, workerGroup)
                    //channel在netty里面是一大核心概念，可以理解为一条channel就是一个连接或者一个服务端bind动作
                    .channel(NioServerSocketChannel.class)
                    //表示服务器启动过程中，需要经过哪些流程，这里SimpleServerHandler最终的顶层接口为ChannelHander，是netty的一大核心概念，表示数据流经过的处理器，可以理解为流水线上的每一道关卡
                    .handler(new LoggingHandler(LogLevel.INFO))
                    //表示一条新的连接进来之后，该怎么处理，也就是上面所说的，老板如何给工人配活
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            if (sslCtx != null) {
                                p.addLast(sslCtx.newHandler(ch.alloc()));
                            }
                            p.addLast(new DiscardServerHandler());
                        }
                    });
            // 绑定端口，等待服务器启动完毕
            ChannelFuture f = b.bind(PORT).sync();
            // 等待服务端关闭socket
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
