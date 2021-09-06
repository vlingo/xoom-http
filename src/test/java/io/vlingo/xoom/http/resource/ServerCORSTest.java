// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import static io.vlingo.xoom.http.Response.Status.Ok;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.http.CORSResponseFilter;
import io.vlingo.xoom.http.Filters;
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

public class ServerCORSTest extends ResourceTestFixtures {
  private static final Random random = new Random();
  private static final AtomicInteger baseServerPort = new AtomicInteger(10_000 + random.nextInt(50_000));

  private static String headerAcceptOriginAny = "*";
  private static String responseHeaderAcceptAllHeaders = "X-Requested-With, Content-Type, Content-Length";
  private static String responseHeaderAcceptMethodsAll = "POST,GET,PUT,PATCH,DELETE";

  protected int serverPort;

  private ClientRequestResponseChannel client;
  private ResponseChannelConsumer consumer;
  private Progress progress;
  private Server server;

  @Test
  public void testThatServerRespondsWithAccessControlHeaders() {
    final AccessSafely consumeCalls = progress.expectConsumeTimes(1);

    final String request = getUsersOriginHeader();
    client.requestWith(toByteBuffer(request));

    while (consumeCalls.totalWrites() < 1) {
      client.probeChannel();
    }
    consumeCalls.readFrom("completed");

    final Response response = progress.responses.poll();

    assertNotNull(response);
    assertEquals(Ok.name(), response.status.name());
    assertEquals(1, progress.consumeCount.get());

    assertEquals(headerAcceptOriginAny, response.headerValueOr(ResponseHeader.AccessControlAllowOrigin, null));
    assertEquals(responseHeaderAcceptAllHeaders, response.headerValueOr(ResponseHeader.AccessControlAllowHeaders, null));
    assertEquals(responseHeaderAcceptMethodsAll, response.headerValueOr(ResponseHeader.AccessControlAllowMethods, null));
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();

    User.resetId();

    final CORSResponseFilter filter = new CORSResponseFilter();

    final List<ResponseHeader> headers =
            Arrays.asList(
                    ResponseHeader.of(ResponseHeader.AccessControlAllowOrigin, headerAcceptOriginAny),
                    ResponseHeader.of(ResponseHeader.AccessControlAllowHeaders, responseHeaderAcceptAllHeaders),
                    ResponseHeader.of(ResponseHeader.AccessControlAllowMethods, responseHeaderAcceptMethodsAll));

    filter.originHeadersFor(headerAcceptOriginAny, headers);

    final Filters filters = Filters.are(Filters.noRequestFilters(), Arrays.asList(filter));

    serverPort = baseServerPort.getAndIncrement();
    server = Server.startWithAgent(world.stage(), resources, filters, serverPort, 100);
    assertTrue(server.startUp().await(500L));

    progress = new Progress();

    consumer = world.actorFor(ResponseChannelConsumer.class, Definition.has(TestResponseChannelConsumer.class, Definition.parameters(progress)));

    client = new NettyClientRequestResponseChannel(Address.from(Host.of("localhost"), serverPort, AddressType.NONE), consumer, 100, 10240);
  }

  @Override
  @After
  public void tearDown() throws InterruptedException {
    client.close();

    server.shutDown();

    super.tearDown();
  }
}
