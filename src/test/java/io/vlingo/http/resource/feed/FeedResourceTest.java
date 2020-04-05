// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.feed;

import io.vlingo.actors.Definition;
import io.vlingo.actors.World;
import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.http.Response;
import io.vlingo.http.resource.Server;
import io.vlingo.http.resource.TestResponseChannelConsumer;
import io.vlingo.http.resource.TestResponseChannelConsumer.Progress;
import io.vlingo.wire.channel.ResponseChannelConsumer;
import io.vlingo.wire.fdx.bidirectional.ClientRequestResponseChannel;
import io.vlingo.wire.fdx.bidirectional.netty.client.NettyClientRequestResponseChannel;
import io.vlingo.wire.message.ByteBufferAllocator;
import io.vlingo.wire.message.Converters;
import io.vlingo.wire.node.Address;
import io.vlingo.wire.node.AddressType;
import io.vlingo.wire.node.Host;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FeedResourceTest {
  private static final String FeedURI = "/feeds/events";

  private static final AtomicInteger serverPort = new AtomicInteger(17000);

  private final ByteBuffer buffer = ByteBufferAllocator.allocate(65535);
  private ClientRequestResponseChannel client;
  private ResponseChannelConsumer consumer;
  private Progress progress;
  private java.util.Properties properties;
  private Server server;
  private World world;

  @Test
  public void testThatFeedResourceFeeds() {
    final String request = requestFor(FeedURI + "/100");

    client.requestWith(toByteBuffer(request));

    final AccessSafely consumeCalls = progress.expectConsumeTimes(1);

    while (consumeCalls.totalWrites() < 1) {
      client.probeChannel();
    }
    consumeCalls.readFrom("completed");

    final Response contentResponse = progress.responses.poll();

    assertEquals(1, progress.consumeCount.get());
    assertEquals(Response.Status.Ok, contentResponse.status);
    assertEquals("events:100:1\n2\n3\n4\n5\n", contentResponse.entity.content());
  }

  @Before
  public void setUp() throws Exception {
    world = World.startWithDefaults("test-stream-userResource");

    final int testServerPort = serverPort.incrementAndGet();

    properties = new java.util.Properties();
    properties.setProperty("server.http.port", ""+testServerPort);
    properties.setProperty("server.dispatcher.pool", "10");
    properties.setProperty("server.buffer.pool.size", "100");
    properties.setProperty("server.message.buffer.size", "65535");
    properties.setProperty("server.probe.interval", "2");
    properties.setProperty("server.probe.timeout", "2");
    properties.setProperty("server.processor.pool.size", "10");
    properties.setProperty("server.request.missing.content.timeout", "100");

    properties.setProperty("feed.resource.name.events", FeedURI);
    properties.setProperty("feed.resource.events.producer.class", "io.vlingo.http.resource.feed.EventsFeedProducerActor");
    properties.setProperty("feed.resource.events.elements", "5");
    properties.setProperty("feed.resource.events.pool", "10");

    server = Server.startWith(world.stage(), properties);
    assertTrue(server.startUp().await(500L));

    progress = new Progress();
    consumer = world.actorFor(ResponseChannelConsumer.class, Definition.has(TestResponseChannelConsumer.class, Definition.parameters(progress)));
    client = new NettyClientRequestResponseChannel(Address.from(Host.of("localhost"), testServerPort, AddressType.NONE), consumer, 100, 10240);
  }

  private String requestFor(final String filePath) {
    return "GET " + filePath + " HTTP/1.1\nHost: vlingo.io\n\n";
  }

  private ByteBuffer toByteBuffer(final String requestContent) {
    buffer.clear();
    buffer.put(Converters.textToBytes(requestContent));
    buffer.flip();
    return buffer;
  }
}
