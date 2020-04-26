// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.agent;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.vlingo.actors.Logger;

public class HttpAgent {
  private final Channel channel;
  private final EventLoopGroup bossGroup;
  private final EventLoopGroup workerGroup;

  public static HttpAgent initialize(
          final HttpRequestChannelConsumerProvider provider,
          final int port,
          final boolean useSSL,
          final Logger logger)
  throws Exception {

    final SslContext sslContext = useSSL ? sslContext() : null;

    final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    final EventLoopGroup workerGroup = new NioEventLoopGroup();

    ServerBootstrap bootstrap =
            new ServerBootstrap()
              .group(bossGroup, workerGroup)
              .channel(NioServerSocketChannel.class)
              .handler(new LoggingHandler(LogLevel.INFO))
              .childHandler(new AgentInitializer(provider, sslContext, logger));

    return new HttpAgent(bootstrap.bind(port).sync().channel(), bossGroup, workerGroup);
  }

  public void close() {
    channel.close().addListener(outcome -> {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    });
  }

  private static SslContext sslContext() throws Exception {
    final SelfSignedCertificate ssc = new SelfSignedCertificate();
    return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
  }

  private HttpAgent(
          final Channel channel,
          final EventLoopGroup bossGroup,
          final EventLoopGroup workerGroup)
  throws Exception {

    this.channel = channel;
    this.bossGroup = bossGroup;
    this.workerGroup = workerGroup;
  }
}
