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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.Definition;
import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseHeader;
import io.vlingo.http.resource.Server.Sizing;
import io.vlingo.http.resource.Server.Timing;
import io.vlingo.http.resource.TestResponseChannelConsumer.Progress;
import io.vlingo.http.sample.user.model.User;
import io.vlingo.wire.channel.ResponseChannelConsumer;
import io.vlingo.wire.fdx.bidirectional.ClientRequestResponseChannel;
import io.vlingo.wire.node.Address;
import io.vlingo.wire.node.AddressType;
import io.vlingo.wire.node.Host;

public class ServerTest extends ResourceTestFixtures {
  private static final int TOTAL_REQUESTS_RESPONSES = 10_000;
  
  private ClientRequestResponseChannel client;
  private ResponseChannelConsumer consumer;
  private Progress progress;
  private Server server;
  
  @Test
  public void testThatServerDispatchesRequests() throws Exception {
    final String request = postRequest(uniqueJohnDoe());
    client.requestWith(toByteBuffer(request));

    progress.untilConsumed = TestUntil.happenings(1);
    while (progress.untilConsumed.remaining() > 0) {
      client.probeChannel();
    }
    progress.untilConsumed.completes();

    final Response createdResponse = progress.responses.poll();

    assertEquals(1, progress.consumeCount.get());
    assertNotNull(createdResponse.headers.headerOf(ResponseHeader.Location));
    
    final String getUserMessage = "GET " + createdResponse.headerOf(ResponseHeader.Location).value + " HTTP/1.1\nHost: vlingo.io\n\n";

    client.requestWith(toByteBuffer(getUserMessage));
    
    progress.untilConsumed = TestUntil.happenings(1);
    while (progress.untilConsumed.remaining() > 0) {
      client.probeChannel();
    }
    progress.untilConsumed.completes();

    final Response getResponse = progress.responses.poll();

    assertEquals(2, progress.consumeCount.get());
    assertEquals(Response.Ok, getResponse.status);
    assertNotNull(getResponse.entity);
    assertNotNull(getResponse.entity.content);
    assertFalse(getResponse.entity.content.isEmpty());
  }

  @Test
  public void testThatServerDispatchesManyRequests() throws Exception {
    final long startTime = System.currentTimeMillis();
    
    System.out.println("WARNING: THIS TEST RUNS SLOWLY!");
    
    progress.untilConsumed = TestUntil.happenings(TOTAL_REQUESTS_RESPONSES);
    final int totalPairs = TOTAL_REQUESTS_RESPONSES / 2;
    for (int idx = 0; idx < totalPairs; ++idx) {
      final int currentConsumeCount = progress.consumeCount.get();
      client.requestWith(toByteBuffer(postRequest(uniqueJohnDoe())));
      client.requestWith(toByteBuffer(postRequest(uniqueJaneDoe())));
      while (progress.consumeCount.get() < currentConsumeCount) {
        client.probeChannel();
      }
    }

    while (progress.untilConsumed.remaining() > 0) {
      client.probeChannel();
    }
    
    System.out.println("TOTAL REQUESTS-RESPONSES: " + TOTAL_REQUESTS_RESPONSES + " TIME: " + (System.currentTimeMillis() - startTime) + " ms");

    assertEquals(TOTAL_REQUESTS_RESPONSES, progress.consumeCount.get());
    final Response createdResponse = progress.responses.peek();
    assertNotNull(createdResponse.headers.headerOf(ResponseHeader.Location));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();

    User.resetId();

    server = Server.startWith(world.stage(), resources, 8080, new Sizing(2, 100, 10240), new Timing(1, 2, 100));
    Thread.sleep(10); // delay for server startup

    progress = new Progress();
    
    consumer = world.actorFor(Definition.has(TestResponseChannelConsumer.class, Definition.parameters(progress)), ResponseChannelConsumer.class);

    client = new ClientRequestResponseChannel(Address.from(Host.of("localhost"), 8080, AddressType.NONE), consumer, 100, 10240, world.defaultLogger());
  }

  @After
  public void tearDown() {
    client.close();
    
    server.stop();
    
    super.tearDown();
  }
}
