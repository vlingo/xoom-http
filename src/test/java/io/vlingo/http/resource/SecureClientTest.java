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
import org.junit.Test;

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

  @Test
  public void testThatSecureClientReceivesResponse() throws Exception {
    final TestResponseConsumer responseConsumer = new TestResponseConsumer();
    final AccessSafely access = responseConsumer.afterCompleting(1);
    final UnknownResponseConsumer unknown = new UnknownResponseConsumer(access);

    final Client.Configuration config =
            Client.Configuration.secure(
                    world.stage(),
                    Address.from(Host.of("webhook.site"), 443, AddressType.NONE),
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
              .and(URI.create("/0275a8ab-503a-47d4-a045-834bce8767d0"))
              .and(host("webhook.site"))
              .and(connection("close"))
              .and(contentType("text/html"));

    final Completes<Response> response = client.requestWith(request);

    response.andThenConsume(r -> {
      responseContent = r.entity.content();
      access.writeUsing("response", r);
      System.out.println("1.5");
    });

    assertEquals(1, (int) access.readFrom("responseCount"));

    final Response accessResponse = access.readFrom("response");

    assertEquals(responseContent, accessResponse.entity.content());
    System.out.println("RESPONSE CONTENT (1): " + responseContent);
    System.out.println("RESPONSE CONTENT (2): " + accessResponse);
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
