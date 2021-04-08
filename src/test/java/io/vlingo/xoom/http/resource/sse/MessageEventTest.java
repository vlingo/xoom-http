// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource.sse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.ResponseHeader;
import io.vlingo.xoom.http.Header.Headers;

public class MessageEventTest {

  @Test
  public void testThatMessageEventParses() {
    final String event = ": I like events.\nid: 1\nevent: E1\ndata: value\nretry: 2500\n\n";
    final Response response =
            Response.of(
                    Response.Status.Ok,
                    Headers.of(ResponseHeader.contentType("text/event-stream")),
                    event);

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
  public void testThatMultipleMessageEventsParses() {
    final String event1 = ": I like events.\nid: 1\nevent: E1\ndata: value1\nretry: 2500\n\n";
    final String event2 = ": I love events.\nid: 2\nevent: E2\ndata: value2\n\n";
    final String event3 = ": I <3 events.\nid: 3\nevent: E3\ndata: value3\n\n";
    final String event4 = "id\n\n";

    final Response response =
            Response.of(
                    Response.Status.Ok,
                    Headers.of(ResponseHeader.contentType("text/event-stream")),
                    event1 + event2 + event3 + event4);

    final List<MessageEvent> messageEvents = MessageEvent.from(response);

    assertEquals(4, messageEvents.size());

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

    final MessageEvent messageEvent4 = messageEvents.get(3);
    assertTrue(messageEvent4.endOfStream());
  }
}
