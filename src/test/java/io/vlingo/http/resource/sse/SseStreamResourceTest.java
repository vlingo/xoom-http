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

import io.vlingo.actors.World;
import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.RequestHeader;
import io.vlingo.http.resource.Configuration;
import io.vlingo.http.sample.user.AllSseFeedActor;

public class SseStreamResourceTest {
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

    resource.requestResponseContext.channel.untilRespondWith = TestUntil.happenings(10);

    resource.nextRequest(request);

    resource.subscribeToStream("all", AllSseFeedActor.class, 10, 10, "1");
    
    resource.requestResponseContext.channel.untilRespondWith.completes();

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

    resource.requestResponseContext.channel.untilRespondWith = TestUntil.happenings(10);

    resource.nextRequest(subscribe);

    resource.subscribeToStream("all", AllSseFeedActor.class, 1, 10, "1");
    
    resource.requestResponseContext.channel.untilRespondWith.completes();

    assertTrue(1 <= resource.requestResponseContext.channel.respondWithCount.get());

    final String clientId = resource.requestResponseContext.id();

    final Request unsubscribe =
            Request
              .method(Method.DELETE)
              .uri("/eventstreams/all/" + clientId)
              .and(RequestHeader.host("StreamsRUs.co"))
              .and(RequestHeader.accept("text/event-stream"));

    resource.requestResponseContext.channel.untilAbandon = TestUntil.happenings(1);

    resource.nextRequest(unsubscribe);

    resource.unsubscribeFromStream("all", clientId);
    
    resource.requestResponseContext.channel.untilAbandon.completes();

    assertEquals(1, resource.requestResponseContext.channel.abandonCount.get());
  }

  @Before
  public void setUp() {
    world = World.startWithDefaults("test-stream-userResource");
    Configuration.define();
    resource = new MockSseStreamResource(world);
  }
}
