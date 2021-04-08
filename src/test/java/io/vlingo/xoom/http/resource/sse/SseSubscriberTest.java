// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource.sse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.http.resource.Configuration;

public class SseSubscriberTest {
  private SseClient client;
  private MockRequestResponseContext context;

  @Test
  public void testSubscriberPropertiesBehavior() {
    context.channel.expectRespondWith(1);

    final SseSubscriber subscriber = new SseSubscriber("all", client, "123ABC", "42");

    assertNotNull(subscriber.client());
    assertEquals(context.id(), subscriber.id());
    assertEquals("all", subscriber.streamName());
    assertEquals("123ABC", subscriber.correlationId());
    assertEquals("42", subscriber.currentEventId());
    subscriber.currentEventId("4242");
    assertEquals("4242", subscriber.currentEventId());
    assertTrue(subscriber.isCompatibleWith("all"));
    assertFalse(subscriber.isCompatibleWith("amm"));
    assertEquals(0, context.channel.abandonCount.get());
    final AccessSafely abandonSafely = context.channel.expectAbandon(1);
    subscriber.close();
    assertEquals(1, (int) abandonSafely.readFrom("count"));
    assertEquals(1, context.channel.abandonCount.get());
  }

  @Before
  public void setUp() {
    Configuration.define();
    context = new MockRequestResponseContext(new MockResponseSenderChannel());
    client = new SseClient(context);
  }
}
