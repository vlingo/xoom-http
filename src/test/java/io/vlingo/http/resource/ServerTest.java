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

import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseHeader;
import io.vlingo.http.resource.Server.Sizing;
import io.vlingo.http.resource.Server.Timing;
import io.vlingo.wire.fdx.bidirectional.ClientRequestResponseChannel;
import io.vlingo.wire.node.Address;
import io.vlingo.wire.node.AddressType;
import io.vlingo.wire.node.Host;

public class ServerTest extends ResourceTestFixtures {
  private ClientRequestResponseChannel client;
  private MockResponseChannelConsumer consumer;
  private Server server;
  
  @Test
  public void testThatServerDispatchesRequests() throws Exception {
    client.requestWith(toByteBuffer(postJohnDoeUserMessage));
    MockResponseChannelConsumer.untilConsumed = TestUntil.happenings(1);
    while (MockResponseChannelConsumer.untilConsumed.remaining() > 0) {
      client.probeChannel();
    }
    MockResponseChannelConsumer.untilConsumed.completes();
    
    final Response createdResponse = consumer.responses.get(0);
    
    assertEquals(1, consumer.consumeCount);
    assertNotNull(createdResponse.headers.headerOf(ResponseHeader.Location));
    
    final String getUserMessage = "GET " + createdResponse.headerOf(ResponseHeader.Location).value + " HTTP/1.1\nHost: vlingo.io\n\n";
    client.requestWith(toByteBuffer(getUserMessage));
    
    MockResponseChannelConsumer.untilConsumed = TestUntil.happenings(1);
    while (MockResponseChannelConsumer.untilConsumed.remaining() > 0) {
      client.probeChannel();
    }
    MockResponseChannelConsumer.untilConsumed.completes();
    
    final Response getResponse = consumer.responses.get(1);
    
    assertEquals(2, consumer.consumeCount);
    assertNotNull(getResponse.entity);
    assertNotNull(getResponse.entity.content);
    assertFalse(getResponse.entity.content.isEmpty());
  }
  
  @Test
  public void testThatServerBlastDispatchesRequests() throws Exception {
    MockResponseChannelConsumer.untilConsumed = TestUntil.happenings(20);
    for (int idx = 0; idx < 10; ++idx) {
      client.requestWith(toByteBuffer(postJohnDoeUserMessage));
      client.requestWith(toByteBuffer(postJaneDoeUserMessage));
    }

    while (MockResponseChannelConsumer.untilConsumed.remaining() > 0) {
      client.probeChannel();
    }
    MockResponseChannelConsumer.untilConsumed.completes();

    final Response createdResponse = consumer.responses.get(0);
    
    assertEquals(20, consumer.consumeCount);
    assertNotNull(createdResponse.headers.headerOf(ResponseHeader.Location));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();

    server = Server.startWith(world.stage(), resources, 8080, new Sizing(2, 100, 10240), new Timing(10, 10, 100));
    Thread.sleep(100); // delay for server startup

    consumer = new MockResponseChannelConsumer();

    client = new ClientRequestResponseChannel(Address.from(Host.of("localhost"), 8080, AddressType.NONE), consumer, 10240, world.defaultLogger());
  }

  @After
  public void tearDown() {
    client.close();
    
    server.stop();
    
    super.tearDown();
  }
}
