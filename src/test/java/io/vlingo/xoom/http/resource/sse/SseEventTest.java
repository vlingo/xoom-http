// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource.sse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import io.vlingo.xoom.http.Header.Headers;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.ResponseHeader;

public class SseEventTest {

  @Test
  public void testThatEventBuilds() {
    final SseEvent event =
            SseEvent.Builder.instance()
              .comment("I like events.")
              .id(1)
              .event("E1")
              .data("{ \"name\" : \"value\" }")
              .retry(2500)
              .toEvent();

    assertEquals("I like events.", event.comment);
    assertEquals("1", event.id);
    assertEquals("E1", event.event);
    assertEquals("{ \"name\" : \"value\" }", event.data);
    assertEquals(2500, event.retry);
  }

  @Test
  public void testDefaults() {
    final SseEvent event = SseEvent.Builder.instance().toEvent();

    assertNull(event.comment);
    assertNull(event.id);
    assertFalse(event.hasId());
    assertNull(event.event);
    assertNull(event.data);
    assertEquals(SseEvent.NoRetry, event.retry);
  }

  @Test
  public void testThatMarksEndOfStream() {
    final SseEvent event = SseEvent.Builder.instance().endOfStream().toEvent();

    assertEquals("", event.id);
    assertTrue(event.endOfStream());
    assertTrue(event.id.isEmpty());
  }

  @Test
  public void testThatEventIsSendable() {
    final SseEvent event =
            SseEvent.Builder.instance()
              .comment("I like events.")
              .id(1)
              .event("E1")
              .data("value")
              .retry(2500)
              .toEvent();

    assertEquals(": I like events.\nid: 1\nevent: E1\ndata: value\nretry: 2500\n\n", event.sendable());
  }

  @Test
  public void testThatEventTranslates() {
    final SseEvent event =
            SseEvent.Builder.instance()
              .comment("I like events.")
              .id(1)
              .event("E1")
              .data("value")
              .retry(2500)
              .toEvent();

    final Response response =
            Response.of(
                    Response.Status.Ok,
                    Headers.of(ResponseHeader.contentType("text/event-stream")),
                    event.sendable());

    final List<MessageEvent> messageEvents = MessageEvent.from(response);

    assertEquals(1, messageEvents.size());

    final MessageEvent messageEvent = messageEvents.get(0);

    assertEquals("I like events.", messageEvent.comment);
    assertEquals("1", messageEvent.id);
    assertEquals("E1", messageEvent.event);
    assertEquals("value", messageEvent.data);
    assertEquals(2500, messageEvent.retry);
  }

  @Test
  public void testThatEventTranslatesEndOfStream() {
    final SseEvent event = SseEvent.Builder.instance().endOfStream().toEvent();

    final Response response =
            Response.of(
                    Response.Status.Ok,
                    Headers.of(ResponseHeader.contentType("text/event-stream")),
                    event.sendable());

    final List<MessageEvent> messageEvents = MessageEvent.from(response);

    assertEquals(1, messageEvents.size());

    final MessageEvent messageEvent = messageEvents.get(0);

    assertTrue(messageEvent.endOfStream());
  }

  @Test
  public void testThatEventTranslatesEndOfStreamWithComment() {
    final SseEvent event = SseEvent.Builder.instance().comment("EOS").endOfStream().toEvent();

    final Response response =
            Response.of(
                    Response.Status.Ok,
                    Headers.of(ResponseHeader.contentType("text/event-stream")),
                    event.sendable());

    final List<MessageEvent> messageEvents = MessageEvent.from(response);

    assertEquals(1, messageEvents.size());

    final MessageEvent messageEvent = messageEvents.get(0);

    assertTrue(messageEvent.endOfStream());
    assertEquals("EOS", messageEvent.comment);
  }
}
