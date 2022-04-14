// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource.sse;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.resource.Configuration;
import io.vlingo.xoom.http.sample.user.AllSseFeedActor;

public class SseFeedTest {
  private SseClient client;
  private MockRequestResponseContext context;
  private SseFeed feed;
  private World world;

  @Test
  public void testThatFeedFeedsOneSubscriber() {
    final AccessSafely respondWithSafely = context.channel.expectRespondWith(1);

    feed = world.actorFor(SseFeed.class, Definition.has(AllSseFeedActor.class, Definition.parameters("all", 10, "1")));

    final SseSubscriber subscriber = new SseSubscriber("all", client, "ABC123", "42");

    feed.to(Arrays.asList(subscriber));

    assertEquals(1, (int) respondWithSafely.readFrom("count"));

    assertEquals(1, context.channel.respondWithCount.get());

    final Response eventsResponse = respondWithSafely.readFrom("eventsResponse");

    final List<MessageEvent> events = MessageEvent.from(eventsResponse);

    assertEquals(10, events.size());
  }

  @Test
  public void testThatFeedFeedsMultipleSubscribers() {
    feed = world.actorFor(SseFeed.class, Definition.has(AllSseFeedActor.class, Definition.parameters("all", 10, "1")));

    final SseSubscriber subscriber1 = new SseSubscriber("all", client, "ABC123", "41");
    final SseSubscriber subscriber2 = new SseSubscriber("all", client, "ABC456", "42");
    final SseSubscriber subscriber3 = new SseSubscriber("all", client, "ABC789", "43");

    final AccessSafely respondWithSafely = context.channel.expectRespondWith(3);

    feed.to(Arrays.asList(subscriber1, subscriber2, subscriber3));

    assertEquals(3, (int) respondWithSafely.readFrom("count"));

    assertEquals(3, context.channel.respondWithCount.get());
  }

  @Before
  public void setUp() {
    world = World.startWithDefaults("test-feed");
    Configuration.define();
    context = new MockRequestResponseContext(new MockResponseSenderChannel());
    client = new SseClient(context);
  }

  @After
  public void tearDown() {
    client.close();
    world.terminate();
  }
}
