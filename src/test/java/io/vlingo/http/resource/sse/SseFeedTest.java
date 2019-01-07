// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.sse;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.Definition;
import io.vlingo.actors.World;
import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.http.resource.Configuration;
import io.vlingo.http.sample.user.AllSseFeedActor;

public class SseFeedTest {
  private SseClient client;
  private MockRequestResponseContext context;
  private SseFeed feed;
  private World world;

  @Test
  public void testThatFeedFeedsOneSubscriber() {
    feed = world.actorFor(SseFeed.class, Definition.has(AllSseFeedActor.class, Definition.parameters("all", 10, "1")));

    final SseSubscriber subscriber = new SseSubscriber("all", client, "ABC123", "42");

    context.channel.untilRespondWith = TestUntil.happenings(1);

    feed.to(Arrays.asList(subscriber));

    context.channel.untilRespondWith.completes();

    assertEquals(1, context.channel.respondWithCount.get());

    final List<MessageEvent> events = MessageEvent.from(context.channel.response.get());

    assertEquals(10, events.size());
  }

  @Test
  public void testThatFeedFeedsMultipleSubscribers() {
    feed = world.actorFor(SseFeed.class, Definition.has(AllSseFeedActor.class, Definition.parameters("all", 10, "1")));

    final SseSubscriber subscriber1 = new SseSubscriber("all", client, "ABC123", "41");
    final SseSubscriber subscriber2 = new SseSubscriber("all", client, "ABC456", "42");
    final SseSubscriber subscriber3 = new SseSubscriber("all", client, "ABC789", "43");

    context.channel.untilRespondWith = TestUntil.happenings(3);

    feed.to(Arrays.asList(subscriber1, subscriber2, subscriber3));

    context.channel.untilRespondWith.completes();

    assertEquals(3, context.channel.respondWithCount.get());
  }

  @Before
  public void setUp() {
    world = World.startWithDefaults("test-feed");
    Configuration.define();
    context = new MockRequestResponseContext(new MockResponseSenderChannel());
    client = new SseClient(context);
  }
}
