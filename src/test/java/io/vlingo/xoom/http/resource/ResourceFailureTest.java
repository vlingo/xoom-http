// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.http.Filters;
import io.vlingo.xoom.http.Request;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.resource.Client.ClientConsumerType;
import io.vlingo.xoom.http.resource.Configuration.Sizing;
import io.vlingo.xoom.http.resource.Configuration.Timing;
import io.vlingo.xoom.http.resource.TestResponseConsumer.UnknownResponseConsumer;
import io.vlingo.xoom.wire.message.ByteBufferAllocator;
import io.vlingo.xoom.wire.message.Converters;
import io.vlingo.xoom.wire.node.Address;
import io.vlingo.xoom.wire.node.AddressType;
import io.vlingo.xoom.wire.node.Host;

public class ResourceFailureTest {
  private static final AtomicInteger nextPort = new AtomicInteger(14000);

  private final ByteBuffer buffer = ByteBufferAllocator.allocate(1024);
  private Client client;
  @SuppressWarnings("unused")
  private int count;
  private int port;
  private FailResource resource;
  @SuppressWarnings("unused")
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

    final Completes<Response> completes1 = client.requestWith(request);

    final Response response = completes1.await();

    Assert.assertNotNull(response);
    Assert.assertEquals(response.status, Response.Status.BadRequest);
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
  public void tearDown() throws InterruptedException {
    client.close();

    server.shutDown();

    Thread.sleep(200);

    world.terminate();
  }

  private ByteBuffer toByteBuffer(final String requestContent) {
    buffer.clear();
    buffer.put(Converters.textToBytes(requestContent));
    buffer.flip();
    return buffer;
  }
}
