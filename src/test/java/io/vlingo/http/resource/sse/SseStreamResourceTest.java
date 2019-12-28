// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.sse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.ActorInstantiator;
import io.vlingo.actors.ActorInstantiator.Registry;
import io.vlingo.actors.World;
import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.http.Context;
import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.RequestHeader;
import io.vlingo.http.resource.Configuration;
import io.vlingo.http.resource.MockCompletesEventuallyResponse;
import io.vlingo.http.sample.user.AllSseFeedActor;

public class SseStreamResourceTest {
  @SuppressWarnings("unused")
  private SseClient client;
  private MockRequestResponseContext context;
  private MockSseStreamResource resource;
  private World world;

  @Test
  public void testThatClientSubscribes() {
    final Request request =
            Request
              .method(Method.GET)
              .uri("/eventstreams/all")
              .and(RequestHeader.host("StreamsRUs.co"))
              .and(RequestHeader.accept("text/event-stream"));

    final AccessSafely respondWithSafely = resource.requestResponseContext.channel.expectRespondWith(10);

    resource.nextRequest(request);

    resource.subscribeToStream("all", AllSseFeedActor.class, 10, 10, "1");

    assertEquals(10, (int) respondWithSafely.readFrom("count"));

    assertEquals(10, resource.requestResponseContext.channel.respondWithCount.get());
  }

  @Test
  public void testThatClientUnsubscribes() {
    final Request subscribe =
            Request
              .method(Method.GET)
              .uri("/eventstreams/all")
              .and(RequestHeader.host("StreamsRUs.co"))
              .and(RequestHeader.accept("text/event-stream"));

    final AccessSafely respondWithSafely = resource.requestResponseContext.channel.expectRespondWith(10);

    resource.nextRequest(subscribe);

    resource.subscribeToStream("all", AllSseFeedActor.class, 1, 10, "1");

    assertTrue(1 <= (int) respondWithSafely.readFrom("count"));
    assertTrue(1 <= resource.requestResponseContext.channel.respondWithCount.get());

    final String clientId = resource.requestResponseContext.id();

    final Request unsubscribe =
            Request
              .method(Method.DELETE)
              .uri("/eventstreams/all/" + clientId)
              .and(RequestHeader.host("StreamsRUs.co"))
              .and(RequestHeader.accept("text/event-stream"));

    final AccessSafely abandonSafely = resource.requestResponseContext.channel.expectAbandon(1);

    resource.nextRequest(unsubscribe);

    resource.unsubscribeFromStream("all", clientId);

    assertEquals(1, (int) abandonSafely.readFrom("count"));
    assertEquals(1, resource.requestResponseContext.channel.abandonCount.get());
  }

  @Test
  public void testThatFeedWithInstantiatorFeeds() {
    final MockCompletesEventuallyResponse completes = new MockCompletesEventuallyResponse();
    final AccessSafely respondWithSafely = context.channel.expectRespondWith(1);

    // NOTE: Any reference to AllSseFeedActor automatically
    // causes the AllSseFeedInstantiator to be registered

    // somehow the static initializer doesn't work for (this) JUnit tests
    @SuppressWarnings({ "unused", "rawtypes" })
    final Class c = AllSseFeedActor.class;  // not registered
    AllSseFeedActor.registerInstantiator(); // registered

    final Request request =
            Request
              .method(Method.GET)
              .uri("/eventstreams/all")
              .and(RequestHeader.host("StreamsRUs.co"))
              .and(RequestHeader.accept("text/event-stream"));

    final SseStreamResource sseStreamResource = new SseStreamResource(world);
    final Context requestContext = new Context(context, request, completes);
    sseStreamResource.__internal__test_set_up(requestContext, world.stage());

    final ActorInstantiator<?> instantiator = Registry.instantiatorFor(AllSseFeedActor.class);
    instantiator.set("feedClass", AllSseFeedActor.class);
    instantiator.set("streamName", "all");
    instantiator.set("feedPayload", 1);
    instantiator.set("feedDefaultId", "123");

    sseStreamResource.subscribeToStream("all", AllSseFeedActor.class, 1, 100, "123");

    final int completedCount = respondWithSafely.readFrom("count");
    assertEquals(1, completedCount);
  }

  @Before
  public void setUp() {
    world = World.startWithDefaults("test-stream-userResource");
    Configuration.define();
    resource = new MockSseStreamResource(world);
    Configuration.define();
    context = new MockRequestResponseContext(new MockResponseSenderChannel());
    client = new SseClient(context);
  }
}
