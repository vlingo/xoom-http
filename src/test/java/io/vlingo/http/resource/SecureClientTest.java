// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import static io.vlingo.http.Method.GET;
import static io.vlingo.http.RequestHeader.connection;
import static io.vlingo.http.RequestHeader.contentType;
import static io.vlingo.http.RequestHeader.host;
import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.After;
import org.junit.Before;

import io.vlingo.actors.World;
import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.common.Completes;
import io.vlingo.http.Request;
import io.vlingo.http.Response;
import io.vlingo.http.resource.TestResponseConsumer.UnknownResponseConsumer;
import io.vlingo.wire.node.Address;
import io.vlingo.wire.node.AddressType;
import io.vlingo.wire.node.Host;

public class SecureClientTest {
  private Client client;
  private String responseContent;
  private World world;

//  @Test
  public void testThatSecureClientReceivesResponse() throws Exception {
    final TestResponseConsumer responseConsumer = new TestResponseConsumer();
    final AccessSafely access = responseConsumer.afterCompleting(1);
    final UnknownResponseConsumer unknown = new UnknownResponseConsumer(access);

    final Client.Configuration config =
            Client.Configuration.secure(
                    world.stage(),
                    Address.from(Host.of("www.google.com"), 443, AddressType.NONE),
                    unknown,
                    false,
                    10,
                    65535,
                    10,
                    65535);

    config.testInfo(true);

    final Client client =
            Client.using(
                    config,
                    Client.ClientConsumerType.RoundRobin,
                    5);

    final Request request =
            Request
              .has(GET)
              .and(URI.create("/"))
              .and(host("www.google.com"))
              .and(connection("close"))
              .and(contentType("text/html"));

System.out.println("0: " + request);
    final Completes<Response> response = client.requestWith(request);
System.out.println("1: " + response);
    response.andThen(r -> { responseContent = r.entity.content; System.out.println("1.5"); return r;} );
System.out.println("2");
    assertEquals(1, (int) access.readFrom("responseCount"));
System.out.println("3");
    final String accessResponseContent = access.readFrom("response");
System.out.println("4");
    assertEquals(accessResponseContent, responseContent);
System.out.println("5");
    System.out.println("RESPONSE CONTENT (1): " + responseContent);
    System.out.println("RESPONSE CONTENT (2): " + accessResponseContent);
  }

  @Before
  public void setUp() {
    world = World.startWithDefaults("secure-client");
  }

  @After
  public void tearDown() {
    if (client != null) client.close();
    if (world != null) world.terminate();
  }
}
