// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import io.vlingo.actors.Definition;
import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseHeader;
import io.vlingo.http.resource.Configuration.Sizing;
import io.vlingo.http.resource.Configuration.Timing;
import io.vlingo.http.resource.TestResponseChannelConsumer.Progress;
import io.vlingo.http.sample.user.model.User;
import io.vlingo.wire.channel.ResponseChannelConsumer;
import io.vlingo.wire.fdx.bidirectional.BasicClientRequestResponseChannel;
import io.vlingo.wire.fdx.bidirectional.ClientRequestResponseChannel;
import io.vlingo.wire.node.Address;
import io.vlingo.wire.node.AddressType;
import io.vlingo.wire.node.Host;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static io.vlingo.http.Response.Status.Ok;
import static io.vlingo.http.Response.Status.PermanentRedirect;
import static org.junit.Assert.*;

public class ServerTest extends ResourceTestFixtures {
  private static final int TOTAL_REQUESTS_RESPONSES = 1_000;

  private static final AtomicInteger baseServerPort = new AtomicInteger(18080);

  private ClientRequestResponseChannel client;
  private ResponseChannelConsumer consumer;
  private int serverPort;
  private Progress progress;
  private Server server;


  @Test
  public void testThatServerHandlesThrowables() {
    final String request = getExceptionRequest("1");
    client.requestWith(toByteBuffer(request));

    final AccessSafely consumeCalls = progress.expectConsumeTimes(1);
    while (consumeCalls.totalWrites() < 1) {
      client.probeChannel();
    }
    consumeCalls.readFrom("completed");

    final Response createdResponse = progress.responses.poll();

    assertEquals(1, progress.consumeCount.get());
    assertEquals(Response.Status.InternalServerError, createdResponse.status);
  }

  @Test
  public void testThatServerDispatchesRequests() throws Exception {
    final String request = postRequest(uniqueJohnDoe());
    client.requestWith(toByteBuffer(request));

    final AccessSafely consumeCalls = progress.expectConsumeTimes(1);
    while (consumeCalls.totalWrites() < 1) {
      client.probeChannel();
    }
    consumeCalls.readFrom("completed");

    final Response createdResponse = progress.responses.poll();

    assertEquals(1, progress.consumeCount.get());
    assertNotNull(createdResponse.headers.headerOf(ResponseHeader.Location));

    final String getUserMessage = "GET " + createdResponse.headerOf(ResponseHeader.Location).value + " HTTP/1.1\nHost: vlingo.io\n\n";

    client.requestWith(toByteBuffer(getUserMessage));

    final AccessSafely moreConsumeCalls = progress.expectConsumeTimes(1);
    while (moreConsumeCalls.totalWrites() < 1) {
      client.probeChannel();
    }
    moreConsumeCalls.readFrom("completed");

    final Response getResponse = progress.responses.poll();

    assertEquals(2, progress.consumeCount.get());
    assertEquals(Response.Status.Ok, getResponse.status);
    assertNotNull(getResponse.entity);
    assertNotNull(getResponse.entity.content());
    assertTrue(getResponse.entity.hasContent());
  }

  @Test
  public void testThatServerDispatchesManyRequests() throws Exception {
    final long startTime = System.currentTimeMillis();

    final AccessSafely consumeCalls = progress.expectConsumeTimes(TOTAL_REQUESTS_RESPONSES);
    final int totalPairs = TOTAL_REQUESTS_RESPONSES / 2;
    int currentConsumeCount = 0;
    for (int idx = 0; idx < totalPairs; ++idx) {
      client.requestWith(toByteBuffer(postRequest(uniqueJohnDoe())));
      client.requestWith(toByteBuffer(postRequest(uniqueJaneDoe())));
      final int expected = currentConsumeCount + 2;
      while (consumeCalls.totalWrites() < expected) {
        client.probeChannel();
      }
      currentConsumeCount = expected;
    }

    while (consumeCalls.totalWrites() < TOTAL_REQUESTS_RESPONSES) {
      client.probeChannel();
    }

    consumeCalls.readFrom("completed");

    System.out.println("TOTAL REQUESTS-RESPONSES: " + TOTAL_REQUESTS_RESPONSES + " TIME: " + (System.currentTimeMillis() - startTime) + " ms");

    assertEquals(TOTAL_REQUESTS_RESPONSES, progress.consumeCount.get());
    final Response createdResponse = progress.responses.peek();
    assertNotNull(createdResponse.headers.headerOf(ResponseHeader.Location));
  }

  @Test
  public void testThatServerRespondsPermanentRedirectWithNoContentLengthHeader() {
    final String request = putRequest("u-123", uniqueJohnDoe());
    client.requestWith(toByteBuffer(request));

    final AccessSafely consumeCalls = progress.expectConsumeTimes(1);
    while (consumeCalls.totalWrites() < 1) {
      client.probeChannel();
    }
    consumeCalls.readFrom("completed");

    final Response response = progress.responses.poll();

    assertNotNull(response);
    assertEquals(PermanentRedirect.name(), response.status.name());
    assertEquals(1, progress.consumeCount.get());
  }

  @Test
  public void testThatServerRespondsOkWithNoContentLengthHeader() {
    final String request = putRequest("u-456", uniqueJohnDoe());
    client.requestWith(toByteBuffer(request));

    final AccessSafely consumeCalls = progress.expectConsumeTimes(1);
    while (consumeCalls.totalWrites() < 1) {
      client.probeChannel();
    }
    consumeCalls.readFrom("completed");

    final Response response = progress.responses.poll();

    assertNotNull(response);
    assertEquals(Ok.name(), response.status.name());
    assertEquals(1, progress.consumeCount.get());
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();

    User.resetId();

    serverPort = baseServerPort.getAndIncrement();
    server = Server.startWith(world.stage(), resources, serverPort, new Sizing(1, 1, 100, 10240), new Timing(1, 1, 100));
    assertTrue(server.startUp().await(500L));

    progress = new Progress();

    consumer = world.actorFor(ResponseChannelConsumer.class, Definition.has(TestResponseChannelConsumer.class, Definition.parameters(progress)));

    client = new BasicClientRequestResponseChannel(Address.from(Host.of("localhost"), serverPort, AddressType.NONE), consumer, 100, 10240, world.defaultLogger());
  }

  @Override
  @After
  public void tearDown() {
    client.close();

    server.shutDown(); // TODO: wait
//    if (!server.shutDown().await(2000)) {
//      System.out.println("Server did not shut down properly.");
//    }

    super.tearDown();
  }
}
