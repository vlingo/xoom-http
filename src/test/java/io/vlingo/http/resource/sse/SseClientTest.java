// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.sse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseHeader;
import io.vlingo.http.resource.Configuration;

public class SseClientTest {
  private SseClient client;
  private MockRequestResponseContext context;

  @Test
  public void testThatClientCloses() {
    final AccessSafely abandonSafely = context.channel.expectAbandon(1);

    client.close();

    assertEquals(1, (int) abandonSafely.readFrom("count"));
    assertEquals(1, context.channel.abandonCount.get());
  }

  @Test
  public void testThatClientHasId() {
    assertEquals(context.id(), client.id());
  }

  @Test
  public void testThatSingleEventSends() {
    final AccessSafely respondWithSafely = context.channel.expectRespondWith(1);

    final SseEvent event =
            SseEvent.Builder.instance()
              .comment("I like events.")
              .id(1)
              .event("E1")
              .data("value")
              .retry(2500)
              .toEvent();

    client.send(event);

    assertEquals(1, (int) respondWithSafely.readFrom("count"));
    assertEquals(1, context.channel.respondWithCount.get());

    final Response response = context.channel.response.get();
    assertNotNull(response);
    assertNotNull(response.headerOf(ResponseHeader.Connection));
    assertNotNull(response.headerOf(ResponseHeader.ContentType));
    assertNotNull(response.headerOf(ResponseHeader.CacheControl));

    final Response eventsResponse = respondWithSafely.readFrom("eventsResponse");

    final List<MessageEvent> messageEvents = MessageEvent.from(eventsResponse);

    assertEquals(1, messageEvents.size());

    final MessageEvent messageEvent = messageEvents.get(0);

    assertEquals("I like events.", messageEvent.comment);
    assertEquals("1", messageEvent.id);
    assertEquals("E1", messageEvent.event);
    assertEquals("value", messageEvent.data);
    assertEquals(2500, messageEvent.retry);
  }

  @Test
  public void testThatMultipleEventsSends() {
    final AccessSafely respondWithSafely = context.channel.expectRespondWith(1);

    final SseEvent event1 =
            SseEvent.Builder.instance()
              .comment("I like events.")
              .id(1)
              .event("E1")
              .data("value1")
              .retry(2500)
              .toEvent();

    final SseEvent event2 =
            SseEvent.Builder.instance()
              .comment("I love events.")
              .id(2)
              .event("E2")
              .data("value2")
              .toEvent();

    final SseEvent event3 =
            SseEvent.Builder.instance()
              .comment("I <3 events.")
              .id(3)
              .event("E3")
              .data("value3")
              .toEvent();

    client.send(event1, event2, event3);

    assertEquals(1, (int) respondWithSafely.readFrom("count"));
    assertEquals(1, context.channel.respondWithCount.get());

    final Response response = context.channel.response.get();
    assertNotNull(response);
    assertNotNull(response.headerOf(ResponseHeader.Connection));
    assertNotNull(response.headerOf(ResponseHeader.ContentType));
    assertNotNull(response.headerOf(ResponseHeader.CacheControl));

    final Response eventsResponse = respondWithSafely.readFrom("eventsResponse");

    final List<MessageEvent> messageEvents = MessageEvent.from(eventsResponse);

    assertEquals(3, messageEvents.size());

    final MessageEvent messageEvent1 = messageEvents.get(0);
    assertEquals("I like events.", messageEvent1.comment);
    assertEquals("1", messageEvent1.id);
    assertEquals("E1", messageEvent1.event);
    assertEquals("value1", messageEvent1.data);
    assertEquals(2500, messageEvent1.retry);

    final MessageEvent messageEvent2 = messageEvents.get(1);
    assertEquals("I love events.", messageEvent2.comment);
    assertEquals("2", messageEvent2.id);
    assertEquals("E2", messageEvent2.event);
    assertEquals("value2", messageEvent2.data);
    assertEquals(MessageEvent.NoRetry, messageEvent2.retry);

    final MessageEvent messageEvent3 = messageEvents.get(2);
    assertEquals("I <3 events.", messageEvent3.comment);
    assertEquals("3", messageEvent3.id);
    assertEquals("E3", messageEvent3.event);
    assertEquals("value3", messageEvent3.data);
    assertEquals(MessageEvent.NoRetry, messageEvent3.retry);
  }

  @Test
  public void testThatEndOfStreamSends() {
    final AccessSafely respondWithSafely = context.channel.expectRespondWith(1);

    final SseEvent event = SseEvent.Builder.instance().endOfStream().toEvent();

    client.send(event);

    assertEquals(1, (int) respondWithSafely.readFrom("count"));
    assertEquals(1, context.channel.respondWithCount.get());

    final Response response = context.channel.response.get();
    assertNotNull(response);

    final Response eventsResponse = respondWithSafely.readFrom("eventsResponse");

    final List<MessageEvent> messageEvents = MessageEvent.from(eventsResponse);

    assertEquals(1, messageEvents.size());

    final MessageEvent messageEvent = messageEvents.get(0);

    assertTrue(messageEvent.endOfStream());
  }

  @Before
  public void setUp() {
    Configuration.define();
    context = new MockRequestResponseContext(new MockResponseSenderChannel());
    client = new SseClient(context);
  }
}
