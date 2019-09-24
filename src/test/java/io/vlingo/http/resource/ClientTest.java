// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import static io.vlingo.http.Method.POST;
import static io.vlingo.http.RequestHeader.contentLength;
import static io.vlingo.http.RequestHeader.host;
import static io.vlingo.http.Response.Status.Created;
import static io.vlingo.http.Response.Status.RequestTimeout;
import static io.vlingo.http.ResponseHeader.Location;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.http.Body;
import io.vlingo.http.Request;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseHeader;
import io.vlingo.http.resource.Client.Configuration;
import io.vlingo.http.resource.Configuration.Sizing;
import io.vlingo.http.resource.Configuration.Timing;
import io.vlingo.http.resource.TestResponseConsumer.KnownResponseConsumer;
import io.vlingo.http.resource.TestResponseConsumer.UnknownResponseConsumer;
import io.vlingo.http.sample.user.model.User;

public class ClientTest extends ResourceTestFixtures {
  private Client client;
  private int expectedHeaderCount;
  private Response expectedResponse;
  private ResponseHeader location;
  private Server server;

  @Test
  public void testThatCorrelatingClientDelivers() throws Exception {
    final String user = johnDoeUserSerialized;

    final TestResponseConsumer safely = new TestResponseConsumer();
    final AccessSafely access = safely.afterCompleting(1);
    final UnknownResponseConsumer unknown = new UnknownResponseConsumer(access);
    final KnownResponseConsumer known = new KnownResponseConsumer(access);

    client = Client.using(Configuration.defaultedExceptFor(world.stage(), unknown));

    client.requestWith(
            Request
              .has(POST)
              .and(URI.create("/users"))
              .and(host("localhost"))
              .and(contentLength(user))
              .and(Body.from(user)))
          .andThenConsume(5000, Response.of(RequestTimeout), response -> expectedResponse = response)
          .andThenConsume(response -> expectedHeaderCount = response.headers.size())
          .andThenConsume(response -> location = response.headers.headerOf(Location))
          .andFinallyConsume(known::consume);

    final int responseCount = access.readFrom("responseCount");
    final int unknownResponseCount = access.readFrom("unknownResponseCount");

    assertEquals(1, responseCount);
    assertNotNull(expectedResponse);
    assertEquals(Created, expectedResponse.status);
    assertEquals(3, expectedHeaderCount);
    assertNotNull(location);
    assertEquals(0, unknownResponseCount);
  }

  @Test
  public void testThatRoundRobinClientDelivers() throws Exception {
    final TestResponseConsumer safely = new TestResponseConsumer();
    final AccessSafely access = safely.afterCompleting(10);
    final UnknownResponseConsumer unknown = new UnknownResponseConsumer(access);
    final KnownResponseConsumer known = new KnownResponseConsumer(access);

    final Configuration config = Client.Configuration.defaultedExceptFor(world.stage(), unknown);
    config.testInfo(true);

    final Client client =
            Client.using(
                    config,
                    Client.ClientConsumerType.RoundRobin,
                    5);

    for (int count = 0; count < 100; ++count) {
      final String user = count % 2 == 0 ? uniqueJohnDoe() : uniqueJaneDoe();
      client.requestWith(
              Request
                .has(POST)
                .and(URI.create("/users"))
                .and(host("localhost"))
                .and(contentLength(user))
                .and(Body.from(user)))
            .andFinallyConsume(response -> known.consume(response) );
    }

    final int responseCount = access.readFromExpecting("responseCount", 100, 2000);
    final int total = access.readFrom("totalAllResponseCount");
    final int unknownResponseCount = access.readFrom("unknownResponseCount");
    final Map<String,Integer> clientCounts = access.readFrom("responseClientCounts");

    assertEquals(100, total);
    assertEquals(100, responseCount);
    assertEquals(0, unknownResponseCount);

    for (final String id : clientCounts.keySet()) {
      final int clientCound = clientCounts.get(id);
      assertEquals(20, clientCound);
    }
  }

  @Test
  public void testThatLoadBalancingClientDelivers() throws Exception {
    final TestResponseConsumer safely = new TestResponseConsumer();
    final AccessSafely access = safely.afterCompleting(100);
    final UnknownResponseConsumer unknown = new UnknownResponseConsumer(access);
    final KnownResponseConsumer known = new KnownResponseConsumer(access);

    final Configuration config = Client.Configuration.defaultedExceptFor(world.stage(), unknown);
    config.testInfo(true);

    final Client client =
            Client.using(
                    config,
                    Client.ClientConsumerType.LoadBalancing,
                    5);

    for (int count = 0; count < 100; ++count) {
      final String user = count % 2 == 0 ? uniqueJohnDoe() : uniqueJaneDoe();
      client.requestWith(
              Request
                .has(POST)
                .and(URI.create("/users"))
                .and(host("localhost"))
                .and(contentLength(user))
                .and(Body.from(user)))
            .andFinallyConsume(known::consume);
    }

    final int responseCount = access.readFromExpecting("responseCount", 100, 2000);
    final int total = access.readFrom("totalAllResponseCount");
    final int unknownResponseCount = access.readFrom("unknownResponseCount");
    final Map<String,Integer> clientCounts = access.readFrom("responseClientCounts");

    assertEquals(100, total);
    assertEquals(100, responseCount);
    assertEquals(0, unknownResponseCount);

    int totalClientCounts = 0;
    for (final String id : clientCounts.keySet()) {
      final int clientCound = clientCounts.get(id);
      totalClientCounts += clientCound;
    }
    assertEquals(100, totalClientCounts);
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();

    User.resetId();

    server = Server.startWith(world.stage(), resources, 8080, new Sizing(1, 10, 100, 10240), new Timing(1, 100));

    Thread.sleep(10); // delay for server startup
  }

  @Override
  @After
  public void tearDown() {
    if (client != null) client.close();

    if (server != null) server.stop();

    super.tearDown();
  }
}
