// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.World;

public class SecureClientTest {
  private Client client;
  //private String responseContent;
  private World world;

  @Test
  public void testThatSecureClientReceivesResponse() throws Exception {
//    final TestResponseConsumer responseConsumer = new TestResponseConsumer();
//    final AccessSafely access = responseConsumer.afterCompleting(1);
//    final UnknownResponseConsumer unknown = new UnknownResponseConsumer(access);
//
//    final Configuration config =
//            Client.Configuration.secure(
//                    world.stage(),
//                    Address.from(Host.of("google.com"), 443, AddressType.NONE),
//                    unknown,
//                    false,
//                    1000,
//                    65535,
//                    10,
//                    65535);
//
//    config.testInfo(true);
//
//    final Client client =
//            Client.using(
//                    config,
//                    Client.ClientConsumerType.RoundRobin,
//                    5);
//
//    final Request request =
//            Request
//              .has(GET)
//              .and(URI.create("/"))
//              .and(host("google.com"))
//              .and(contentType("text/html"));
//
//    final Completes<Response> response = client.requestWith(request);
//
//    response.andThen(r -> { responseContent = r.entity.content; return r;} );
//
//    assertEquals(1, (int) access.readFrom("responseCount"));
//
//    final String accessResponseContent = access.readFrom("response");
//
//    assertEquals(accessResponseContent, responseContent);
//
//    System.out.println("RESPONSE CONTENT (1): " + responseContent);
//    System.out.println("RESPONSE CONTENT (2): " + accessResponseContent);
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
