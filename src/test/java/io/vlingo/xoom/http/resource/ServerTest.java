// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import static io.vlingo.xoom.http.Response.Status.Ok;
import static io.vlingo.xoom.http.Response.Status.PermanentRedirect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.ResponseHeader;
import io.vlingo.xoom.http.resource.TestResponseChannelConsumer.Progress;
import io.vlingo.xoom.http.sample.user.model.User;
import io.vlingo.xoom.wire.channel.ResponseChannelConsumer;
import io.vlingo.xoom.wire.fdx.bidirectional.ClientRequestResponseChannel;
import io.vlingo.xoom.wire.fdx.bidirectional.netty.client.NettyClientRequestResponseChannel;
import io.vlingo.xoom.wire.node.Address;
import io.vlingo.xoom.wire.node.AddressType;
import io.vlingo.xoom.wire.node.Host;

public abstract class ServerTest extends ResourceTestFixtures {
  private static final int TOTAL_REQUESTS_RESPONSES = 200;

  private static final Random random = new Random();
  private static final AtomicInteger baseServerPort = new AtomicInteger(10_000 + random.nextInt(50_000));

  protected int serverPort;
  protected boolean skipTests;

  private ClientRequestResponseChannel client;
  private ResponseChannelConsumer consumer;
  private Progress progress;
  private Server server;

  @Test
  public void testThatServerHandlesThrowables() {
    System.out.println(">>>>>>>>>>>>>>>>>>>>> testThatServerHandlesThrowables");

    if (skipTests) {
      System.out.println(">>>>>>>>>>>>>>>>>>>>> skipped");
      return;
    }

    final AccessSafely consumeCalls = progress.expectConsumeTimes(1);

    final String request = getExceptionRequest("1");
    client.requestWith(toByteBuffer(request));

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
    System.out.println(">>>>>>>>>>>>>>>>>>>>> testThatServerDispatchesRequests");

    if (skipTests) {
      System.out.println(">>>>>>>>>>>>>>>>>>>>> skipped");
      return;
    }

    final AccessSafely consumeCalls = progress.expectConsumeTimes(1);

    final String request = postRequest(uniqueJohnDoe());
    client.requestWith(toByteBuffer(request));

    while (consumeCalls.totalWrites() < 1) {
      client.probeChannel();
    }
    consumeCalls.readFrom("completed");

    final Response createdResponse = progress.responses.poll();

    assertEquals(1, progress.consumeCount.get());
    assertNotNull(createdResponse.headers.headerOf(ResponseHeader.Location));

    final String getUserMessage = "GET " + createdResponse.headerOf(ResponseHeader.Location).value + " HTTP/1.1\nHost: vlingo.io\nConnection: keep-alive\n\n";

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
    System.out.println(">>>>>>>>>>>>>>>>>>>>> testThatServerDispatchesManyRequests");

    if (skipTests) {
      System.out.println(">>>>>>>>>>>>>>>>>>>>> skipped");
      return;
    }

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

      Thread.sleep(100);
    }

    while (consumeCalls.totalWrites() < TOTAL_REQUESTS_RESPONSES) {
      client.probeChannel();
    }

    consumeCalls.readFrom("completed");

    System.out.println("TOTAL REQUESTS-RESPONSES: " + TOTAL_REQUESTS_RESPONSES + " TIME: " + (System.currentTimeMillis() - startTime) + " ms");

    assertTrue(TOTAL_REQUESTS_RESPONSES <= progress.consumeCount.get());
    final Response createdResponse = progress.responses.peek();
    assertNotNull(createdResponse.headers.headerOf(ResponseHeader.Location));
  }

  @Test
  public void testThatServerRespondsPermanentRedirect() {
    System.out.println(">>>>>>>>>>>>>>>>>>>>> testThatServerRespondsPermanentRedirect");

    if (skipTests) {
      System.out.println(">>>>>>>>>>>>>>>>>>>>> skipped");
      return;
    }

    final AccessSafely consumeCalls = progress.expectConsumeTimes(1);

    final String request = putRequest("u-123", uniqueJohnDoe());
    client.requestWith(toByteBuffer(request));

    while (consumeCalls.totalWrites() < 1) {
      client.probeChannel();
    }
    consumeCalls.readFrom("completed");

    final Response response = progress.responses.poll();

    assertNotNull(response);
    assertEquals(PermanentRedirect.name(), response.status.name());
    assertEquals(1, progress.consumeCount.get());
    assertEquals("0", response.headers.headerOf("Content-Length").value);
  }

  @Test
  public void testThatServerRespondsOk() {
    System.out.println(">>>>>>>>>>>>>>>>>>>>> testThatServerRespondsOk");

    if (skipTests) {
      System.out.println(">>>>>>>>>>>>>>>>>>>>> skipped");
      return;
    }

    final AccessSafely consumeCalls = progress.expectConsumeTimes(1);

    final String request = putRequest("u-456", uniqueJohnDoe());
    client.requestWith(toByteBuffer(request));

    while (consumeCalls.totalWrites() < 1) {
      client.probeChannel();
    }
    consumeCalls.readFrom("completed");

    final Response response = progress.responses.poll();

    assertNotNull(response);
    assertEquals(Ok.name(), response.status.name());
    assertEquals(1, progress.consumeCount.get());
    assertEquals("0", response.headers.headerOf("Content-Length").value);
  }

  @Test
  public void testThatServerClosesChannelAfterSingleRequest() {
    System.out.println(">>>>>>>>>>>>>>>>>>>>> testThatServerClosesChannelAfterSingleRequest");

    if (skipTests) {
      System.out.println(">>>>>>>>>>>>>>>>>>>>> skipped");
      return;
    }

    int totalResponses = 0;
    final int maxRequests = 10;

    for (int count = 0; count < maxRequests; ++count) {
      final AccessSafely consumeCalls = progress.expectConsumeTimes(1);
      if (count % 2 == 0) {
        client.requestWith(toByteBuffer(postRequestCloseFollowing(uniqueJohnDoe())));
      } else {
        client.requestWith(toByteBuffer(postRequestCloseFollowing(uniqueJaneDoe())));
      }
      System.out.println("1");
      while (consumeCalls.totalWrites() < 1) {
        client.probeChannel();
      }
      totalResponses += (int) consumeCalls.readFrom("completed");
      System.out.println("2: " + totalResponses);

      client.close();

      client = new NettyClientRequestResponseChannel(Address.from(Host.of("localhost"), serverPort, AddressType.NONE), consumer, 100, 10240);
    }

    assertEquals(maxRequests, totalResponses);
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();

    User.resetId();

    skipTests = false;
    serverPort = baseServerPort.getAndIncrement();
    server = startServer();
    assertTrue(server.startUp().await(500L));

    progress = new Progress();

    consumer = world.actorFor(ResponseChannelConsumer.class, Definition.has(TestResponseChannelConsumer.class, Definition.parameters(progress)));

    client = new NettyClientRequestResponseChannel(Address.from(Host.of("localhost"), serverPort, AddressType.NONE), consumer, 100, 10240);
  }

  protected abstract Server startServer();

  @Override
  @After
  public void tearDown() throws InterruptedException {
    client.close();

    server.shutDown();

    super.tearDown();
  }
}
