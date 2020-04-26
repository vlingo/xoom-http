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
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
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
          final int numberOfThreads,
          final Logger logger)
  throws Exception {

    final SslContext sslContext = useSSL ? sslContext() : null;

    final OptimalTransport optimalTransport = optimalTransport(logger);
    final EventLoopGroup bossGroup = eventLoopGroup(optimalTransport, numberOfThreads, logger);
    final EventLoopGroup workerGroup = eventLoopGroup(optimalTransport, logger);

    ServerBootstrap bootstrap =
            new ServerBootstrap()
              .group(bossGroup, workerGroup)
              .channel(serverSocketChannelType(optimalTransport, logger))
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

  private enum OptimalTransport { NIO, Epoll };

  private static EventLoopGroup eventLoopGroup(
          final OptimalTransport optimalTransport,
          final Logger logger) {

    switch (optimalTransport) {
    case Epoll:
      logger.debug("HttpAgent using EpollEventLoopGroup");
      return new EpollEventLoopGroup();
    case NIO:
    default:
      logger.debug("HttpAgent using NioEventLoopGroup");
      return new NioEventLoopGroup();
    }
  }

  private static EventLoopGroup eventLoopGroup(
          final OptimalTransport optimalTransport,
          final int processorPoolSize,
          final Logger logger) {

    switch (optimalTransport) {
    case Epoll:
      logger.debug("HttpAgent using EpollEventLoopGroup " + processorPoolSize);
      return new EpollEventLoopGroup(processorPoolSize);
    case NIO:
    default:
      logger.debug("HttpAgent using NioEventLoopGroup " + processorPoolSize);
      return new NioEventLoopGroup(processorPoolSize);
    }
  }

  private static OptimalTransport optimalTransport(final Logger logger) {
    final String osName = System.getProperty("os.name");

    logger.debug("HttpAgent running on " + osName);

    if (osName.toLowerCase().contains("linux")) {
      return OptimalTransport.Epoll;
    }

    return OptimalTransport.NIO;
  }

  private static Class<? extends ServerSocketChannel> serverSocketChannelType(
          final OptimalTransport optimalTransport,
          final Logger logger) {

    switch (optimalTransport) {
    case Epoll:
      logger.debug("HttpAgent using EpollServerSocketChannel");
      return EpollServerSocketChannel.class;
    case NIO:
    default:
      logger.debug("HttpAgent using NioServerSocketChannel");
      return NioServerSocketChannel.class;
    }
  }
}
