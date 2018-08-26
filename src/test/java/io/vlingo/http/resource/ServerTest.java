// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.Definition;
import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseHeader;
import io.vlingo.http.resource.Configuration.Sizing;
import io.vlingo.http.resource.Configuration.Timing;
import io.vlingo.http.resource.TestResponseChannelConsumer.Progress;
import io.vlingo.http.sample.user.model.User;
import io.vlingo.wire.channel.ResponseChannelConsumer;
import io.vlingo.wire.fdx.bidirectional.ClientRequestResponseChannel;
import io.vlingo.wire.node.Address;
import io.vlingo.wire.node.AddressType;
import io.vlingo.wire.node.Host;

public class ServerTest extends ResourceTestFixtures {
  private static final int TOTAL_REQUESTS_RESPONSES = 1_000;
  
  private static final AtomicInteger baseServerPort = new AtomicInteger(18080);

  private ClientRequestResponseChannel client;
  private ResponseChannelConsumer consumer;
  private int serverPort;
  private Progress progress;
  private Server server;
  
  @Test
  public void testThatServerDispatchesRequests() throws Exception {
    System.out.println("================================ testThatServerDispatchesRequests()");

    int count = 0;

    final String request = postRequest(uniqueJohnDoe());
    client.requestWith(toByteBuffer(request));
    System.out.println("testThatServerDispatchesRequests(): 1");

    progress.untilConsumed = TestUntil.happenings(1);
    while (progress.untilConsumed.remaining() > 0) {
      if (++count % 250 == 0) {
        System.out.print(".");
      }
      client.probeChannel();
    }
    System.out.println("x");
    progress.untilConsumed.completes();
    System.out.println("testThatServerDispatchesRequests(): 2");

    final Response createdResponse = progress.responses.poll();

    assertEquals(1, progress.consumeCount.get());
    assertNotNull(createdResponse.headers.headerOf(ResponseHeader.Location));
    
    final String getUserMessage = "GET " + createdResponse.headerOf(ResponseHeader.Location).value + " HTTP/1.1\nHost: vlingo.io\n\n";

    client.requestWith(toByteBuffer(getUserMessage));
    System.out.println("testThatServerDispatchesRequests(): 3");
    count = 0;
    progress.untilConsumed = TestUntil.happenings(1);
    while (progress.untilConsumed.remaining() > 0) {
      if (++count % 250 == 0) {
        System.out.print(".");
      }
      client.probeChannel();
    }
    System.out.println("x");
    progress.untilConsumed.completes();
    System.out.println("testThatServerDispatchesRequests(): 4");

    final Response getResponse = progress.responses.poll();

    assertEquals(2, progress.consumeCount.get());
    assertEquals(Response.Status.Ok, getResponse.status);
    assertNotNull(getResponse.entity);
    assertNotNull(getResponse.entity.content);
    assertFalse(getResponse.entity.content.isEmpty());
  }

  @Test
  public void testThatServerDispatchesManyRequests() throws Exception {
    System.out.println("================================ testThatServerDispatchesManyRequests()");

    final long startTime = System.currentTimeMillis();
    int count = 0;
    progress.untilConsumed = TestUntil.happenings(TOTAL_REQUESTS_RESPONSES);
    final int totalPairs = TOTAL_REQUESTS_RESPONSES / 2;
    int currentConsumeCount = 0;
    for (int idx = 0; idx < totalPairs; ++idx) {
      if (++count % 250 == 0) {
        System.out.print("o");
      }
      client.requestWith(toByteBuffer(postRequest(uniqueJohnDoe())));
      client.requestWith(toByteBuffer(postRequest(uniqueJaneDoe())));
      final int expected = currentConsumeCount + 2;
      count = 0;
      while (progress.consumeCount.get() < expected) {
        if (++count % 250 == 0) {
          System.out.print("O");
        }
        client.probeChannel();
      }
      currentConsumeCount = expected;
    }
    System.out.println("X");

    count = 0;
    while (progress.untilConsumed.remaining() > 0) {
      if (++count % 250 == 0) {
        System.out.print(".");
      }
      client.probeChannel();
    }
    System.out.println("X");
    
    System.out.println("TOTAL REQUESTS-RESPONSES: " + TOTAL_REQUESTS_RESPONSES + " TIME: " + (System.currentTimeMillis() - startTime) + " ms");

    assertEquals(TOTAL_REQUESTS_RESPONSES, progress.consumeCount.get());
    final Response createdResponse = progress.responses.peek();
    assertNotNull(createdResponse.headers.headerOf(ResponseHeader.Location));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();

    User.resetId();

    serverPort = baseServerPort.getAndIncrement();
    server = Server.startWith(world.stage(), resources, serverPort, new Sizing(10, 10, 100, 10240), new Timing(1, 2, 100));
    Thread.sleep(1000); // delay for server startup

    progress = new Progress();
    
    consumer = world.actorFor(Definition.has(TestResponseChannelConsumer.class, Definition.parameters(progress)), ResponseChannelConsumer.class);

    client = new ClientRequestResponseChannel(Address.from(Host.of("localhost"), serverPort, AddressType.NONE), consumer, 100, 10240, world.defaultLogger());
  }

  @After
  public void tearDown() {
    client.close();

    if (!server.shutDown().await(2000)) {
      System.out.println("Server did not shut down properly.");
    }

    super.tearDown();
  }
}
