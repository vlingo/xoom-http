// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.agent;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;
import io.vlingo.actors.Logger;

public class AgentInitializer extends ChannelInitializer<SocketChannel> {
  private final Logger logger;
  private final HttpRequestChannelConsumerProvider provider;
  private final SslContext sslContext;

  AgentInitializer(final HttpRequestChannelConsumerProvider provider, final SslContext sslContext, final Logger logger) {
    this.provider = provider;
    this.sslContext = sslContext;
    this.logger = logger;
  }

  @Override
  public void initChannel(final SocketChannel channel) {
    final ChannelPipeline channelPipeline = channel.pipeline();

    if (sslContext != null) {
      channelPipeline.addLast(sslContext.newHandler(channel.alloc()));
    }

    channelPipeline.addLast(new HttpRequestDecoder());

    channelPipeline.addLast(new HttpObjectAggregator(1048576));

    channelPipeline.addLast(new HttpResponseEncoder());

    // remove the following comment if you want automatic content compression
    // p.addLast(new HttpContentCompressor());

    System.out.println("CHANNEL PIPELINE: " + channelPipeline);
    System.out.println("        PROVIDER: " + provider);
    System.out.println("          LOGGER: " + logger);

    final AgentHandler agentHandler = new AgentHandler(provider, logger);

    try {
      channelPipeline.addLast(agentHandler);
    } catch (Exception e) {
      System.out.println("     EXCEPTION: " + e.getMessage());
      e.printStackTrace();
      throw e;
    }

    System.out.println("            DONE: " + agentHandler);
  }
}
