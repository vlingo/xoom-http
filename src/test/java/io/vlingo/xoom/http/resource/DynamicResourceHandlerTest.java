// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import static io.vlingo.xoom.http.Response.Status.Ok;
import static io.vlingo.xoom.http.resource.ResourceBuilder.get;
import static io.vlingo.xoom.http.resource.ResourceBuilder.resource;
import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.resource.TestResponseChannelConsumer.Progress;
import io.vlingo.xoom.wire.channel.ResponseChannelConsumer;
import io.vlingo.xoom.wire.fdx.bidirectional.ClientRequestResponseChannel;
import io.vlingo.xoom.wire.fdx.bidirectional.netty.client.NettyClientRequestResponseChannel;
import io.vlingo.xoom.wire.message.ByteBufferAllocator;
import io.vlingo.xoom.wire.message.Converters;
import io.vlingo.xoom.wire.node.Address;
import io.vlingo.xoom.wire.node.AddressType;
import io.vlingo.xoom.wire.node.Host;

public class DynamicResourceHandlerTest {
  private final ByteBuffer buffer = ByteBufferAllocator.allocate(65535);
  private static final Random random = new Random();
  private static final AtomicInteger serverPort = new AtomicInteger(10_000 + random.nextInt(50_000));
  private ClientRequestResponseChannel client;
  private ResponseChannelConsumer consumer;
  private Progress progress;
  private TestResource resource;
  private Server server;
  private World world;

  @Test
  public void testThatBaseIsSet() {
    Assert.assertNotNull(resource);
    Assert.assertNotNull(resource.stage());
    Assert.assertNotNull(resource.logger());
    Assert.assertNotNull(resource.scheduler());
    Assert.assertNull(resource.context());
  }

  @Test
  public void testThatContextIsSet() {
    final String request = getTestRequest();
    client.requestWith(toByteBuffer(request));
    final AccessSafely consumeCalls = progress.expectConsumeTimes(1);
    while (consumeCalls.totalWrites() < 1) {
      client.probeChannel();
    }
    consumeCalls.readFrom("completed");

    final Response createdResponse = progress.responses.poll();

    assertEquals(1, progress.consumeCount.get());
    assertEquals(Response.Status.Ok, createdResponse.status);

    Assert.assertNotNull(resource.context());
    Assert.assertNotNull(resource.context().request());
    Assert.assertEquals("GET", resource.context().request().method.name);
    Assert.assertEquals("/test", resource.context().request().uri.getPath());
  }

  @Before
  public void setUp() {
    final int port = serverPort.getAndIncrement();

    world = World.startWithDefaults("test-dynamic-resource-handler");

    resource = new TestResource(world.stage());

    server = Server.startWithAgent(world.stage(), Resources.are(resource.routes()), port, 2);

    progress = new Progress();

    consumer = world.actorFor(ResponseChannelConsumer.class, Definition.has(TestResponseChannelConsumer.class, Definition.parameters(progress)));

    client = new NettyClientRequestResponseChannel(Address.from(Host.of("localhost"), port, AddressType.NONE), consumer, 100, 10240);
  }

  @After
  public void tearDown() {
    client.close();

    server.shutDown();
  }

  private String getTestRequest() {
    return "GET /test" + " HTTP/1.1\nHost: vlingo.io\nConnection: close\n\n";
  }

  private ByteBuffer toByteBuffer(final String requestContent) {
    buffer.clear();
    buffer.put(Converters.textToBytes(requestContent));
    buffer.flip();
    return buffer;
  }

  private static class TestResource extends DynamicResourceHandler {
    TestResource(final Stage stage) {
      super(stage);
    }

    public Completes<Response> test() {
      return Completes.withSuccess(Response.of(Ok));
    }

    @Override
    public Resource<?> routes() {
      return resource("Hello Resource", this,
              get("/test")
                .handle(this::test));
    }
  }
}
