// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource.sse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.xoom.actors.ActorInstantiator;
import io.vlingo.xoom.actors.ActorInstantiatorRegistry;
import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.http.Context;
import io.vlingo.xoom.http.Method;
import io.vlingo.xoom.http.Request;
import io.vlingo.xoom.http.RequestHeader;
import io.vlingo.xoom.http.resource.Configuration;
import io.vlingo.xoom.http.resource.MockCompletesEventuallyResponse;
import io.vlingo.xoom.http.sample.user.AllSseFeedActor;

import java.util.concurrent.atomic.AtomicInteger;

public class SseStreamResourceTest {
  private static final AtomicInteger nextStreamNumber = new AtomicInteger(0);
  @SuppressWarnings("unused")
  private SseClient client;
  private MockRequestResponseContext context;
  private MockSseStreamResource resource;
  private World world;

  @Test
  public void testThatClientSubscribes() {
    final String streamName = nextStreamName();
    final Request request =
            Request
              .method(Method.GET)
              .uri("/eventstreams/" + streamName)
              .and(RequestHeader.host("StreamsRUs.co"))
              .and(RequestHeader.accept("text/event-stream"));

    final AccessSafely respondWithSafely = resource.requestResponseContext.channel.expectRespondWith(10);

    resource.nextRequest(request);

    resource.subscribeToStream(streamName, AllSseFeedActor.class, 10, 10, "1");

    assertEquals(10, (int) respondWithSafely.readFrom("count"));

    assertEquals(10, resource.requestResponseContext.channel.respondWithCount.get());
  }

  @Test
  public void testThatClientUnsubscribes() {
    final String streamName = nextStreamName();
    final Request subscribe =
            Request
              .method(Method.GET)
              .uri("/eventstreams/" + streamName)
              .and(RequestHeader.host("StreamsRUs.co"))
              .and(RequestHeader.accept("text/event-stream"));

    final AccessSafely respondWithSafely = resource.requestResponseContext.channel.expectRespondWith(10);

    resource.nextRequest(subscribe);

    resource.subscribeToStream(streamName, AllSseFeedActor.class, 1, 10, "1");

    assertTrue(1 <= (int) respondWithSafely.readFrom("count"));
    assertTrue(1 <= resource.requestResponseContext.channel.respondWithCount.get());

    final String clientId = resource.requestResponseContext.id();

    final Request unsubscribe =
            Request
              .method(Method.DELETE)
              .uri("/eventstreams/" + streamName +"/" + clientId)
              .and(RequestHeader.host("StreamsRUs.co"))
              .and(RequestHeader.accept("text/event-stream"));

    final AccessSafely abandonSafely = resource.requestResponseContext.channel.expectAbandon(1);

    resource.nextRequest(unsubscribe);

    resource.unsubscribeFromStream(streamName, clientId);

    assertEquals(1, (int) abandonSafely.readFrom("count"));
    assertEquals(1, resource.requestResponseContext.channel.abandonCount.get());
  }

  @Test
  public void testThatFeedWithInstantiatorFeeds() {
    final String streamName = nextStreamName();
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
              .uri("/eventstreams/" + streamName)
              .and(RequestHeader.host("StreamsRUs.co"))
              .and(RequestHeader.accept("text/event-stream"));

    final SseStreamResource sseStreamResource = new SseStreamResource(world);
    final Context requestContext = new Context(context, request, completes);
    sseStreamResource.__internal__test_set_up(requestContext, world.stage());

    final ActorInstantiator<?> instantiator = ActorInstantiatorRegistry.instantiatorFor(AllSseFeedActor.class);
    instantiator.set("feedClass", AllSseFeedActor.class);
    instantiator.set("streamName", streamName);
    instantiator.set("feedPayload", 1);
    instantiator.set("feedDefaultId", "123");

    sseStreamResource.subscribeToStream(streamName, AllSseFeedActor.class, 1, 100, "123");

    final int completedCount = respondWithSafely.readFrom("count");
    assertEquals(1, completedCount);
  }

  private String nextStreamName() {
    return "all" + "-" + nextStreamNumber.incrementAndGet();
  }

  @Before
  public void setUp() {
    world = World.startWithDefaults("test-stream-userResource");
    Configuration.define();
    resource = new MockSseStreamResource(world);
    context = new MockRequestResponseContext(new MockResponseSenderChannel());
    client = new SseClient(context);
    AllSseFeedActor.registerInstantiator();
  }

  @After
  public void tearDown() {
    client.close();
    world.terminate();
  }
}
