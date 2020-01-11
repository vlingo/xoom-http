// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.World;
import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.http.Filters;
import io.vlingo.http.Request;
import io.vlingo.http.Response;
import io.vlingo.http.resource.Client.ClientConsumerType;
import io.vlingo.http.resource.Configuration.Sizing;
import io.vlingo.http.resource.Configuration.Timing;
import io.vlingo.http.resource.TestResponseConsumer.UnknownResponseConsumer;
import io.vlingo.wire.message.ByteBufferAllocator;
import io.vlingo.wire.message.Converters;
import io.vlingo.wire.node.Address;
import io.vlingo.wire.node.AddressType;
import io.vlingo.wire.node.Host;

public class ResourceFailureTest {
  private static final AtomicInteger nextPort = new AtomicInteger(14000);

  private final ByteBuffer buffer = ByteBufferAllocator.allocate(1024);
  private Client client;
  private int count;
  private int port;
  private FailResource resource;
  private Response response;
  private Server server;
  private World world;

  @Test
  public void testBasicFailire() throws Exception {
    final TestResponseConsumer consumer = new TestResponseConsumer();
    final AccessSafely access = consumer.afterCompleting(1);
    final UnknownResponseConsumer unknown = new UnknownResponseConsumer(access);

    final Client.Configuration config =
            Client.Configuration.defaultedExceptFor(
                    world.stage(),
                    Address.from(Host.of("localhost"), port, AddressType.NONE),
                    unknown);

    client = Client.using(config, ClientConsumerType.RoundRobin, 1);

    final Request request = Request.from(toByteBuffer("GET /fail HTTP/1.1\nHost: vlingo.io\n\n"));

    count = 0;

    client.requestWith(request).andThenConsume(response -> {
      ++count;
      this.response = response;
    }).await();

    Assert.assertEquals(1, count);

    Assert.assertNotNull(response);
  }

  @Before
  public void setUp() {
    world = World.startWithDefaults("test-request-failure");

    resource = new FailResource();

    port = nextPort.incrementAndGet();

    server = Server.startWith(
            world.stage(),
            Resources.are(resource.routes()),
            Filters.none(),
            port,
            Sizing.define(),
            Timing.define());
  }

  @After
  public void tearDown() {
    server.shutDown();

    world.terminate();
  }

  private ByteBuffer toByteBuffer(final String requestContent) {
    buffer.clear();
    buffer.put(Converters.textToBytes(requestContent));
    buffer.flip();
    return buffer;
  }
}
