// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.sse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.vlingo.http.Header;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseHeader;

/**
 * Describes the HTTP body content of SSE messages
 * @see <a href="https://en.wikipedia.org/wiki/Server-sent_events">https://en.wikipedia.org/wiki/Server-sent_events</a>
 * @see <a href="https://html.spec.whatwg.org/multipage/server-sent-events.html">https://html.spec.whatwg.org/multipage/server-sent-events.html</a>
 */
public class MessageEvent {
  public static final int NoRetry = -1;

  public final String comment;
  public final String data;
  public final String event;
  public final String id;
  public final int retry;

  /**
   * On client side received SSE message can be parsed into {@link MessageEvent}
   * @param response input the client read from SSE server
   * @return a parsed list of {@link MessageEvent} - empty event if header is missing and not got correct header
   */
  public static List<MessageEvent> from(final Response response) {
    final Header header = response.headerOf(ResponseHeader.ContentType);
    if (header == null || !header.value.equals("text/event-stream")) {
      Collections.emptyList();
    }

    List<MessageEvent> events = new ArrayList<>(2);

    final String[] rawContent = response.entity.content().split("\n");
    int startIndex = 0;
    int currentIndex = 0;
    boolean isEvent = false;

    for ( ; currentIndex < rawContent.length; ++currentIndex) {
      if (rawContent[currentIndex].length() > 0) {
        if (!isEvent) {
          isEvent = true;
          startIndex = currentIndex;
        }
      } else {
        if (isEvent) {
          isEvent = false;
          events.add(eventFrom(rawContent, startIndex, currentIndex - 1));
          startIndex = 0;
        }
      }
    }

    if (isEvent) {
      events.add(eventFrom(rawContent, startIndex, currentIndex - 1));
    }

    return events;
  }

  private static MessageEvent eventFrom(final String[] rawContent, final int startIndex, final int endIndex) {
    String comment = null;
    String data = null;
    String event = null;
    String id = null;
    int retry = NoRetry;

    for (int currentIndex = startIndex; currentIndex <= endIndex; ++currentIndex) {
      final int colon = rawContent[currentIndex].indexOf(':');
      if (colon > 0) {
        final String field = rawContent[currentIndex].substring(0, colon).trim();
        final String value = rawContent[currentIndex].length() > (colon + 1) ? rawContent[currentIndex].substring(colon + 1).trim() : "";
        switch (field) {
        case "data":
          data = data == null ? value : data + "\n" + value;
          break;
        case "event":
          event = value;
          break;
        case "id":
          id = value;
          break;
        case "retry":
          try { retry = Integer.parseInt(value); } catch (Exception e) { }
          break;
        }
      } else if (colon == 0) {
        if (rawContent[currentIndex].length() > 0) {
          comment = rawContent[currentIndex].substring(1).trim();
        }
      } else {
        switch (rawContent[currentIndex].trim()) {
        case "data":
          break;   // non-data
        case "event":
          break;   // non-event
        case "id":
          id = ""; // end of stream
          break;
        case "retry":
          retry = NoRetry;
          break;
        }
      }
    }

    return new MessageEvent(id, event, data, retry, comment);
  }

  public MessageEvent(final String id, final String event, final String data, final int retry, final String comment) {
    this.id = id;
    this.event = event;
    this.data = data;
    this.retry = retry;
    this.comment = comment;
  }

  public boolean endOfStream() {
    return id != null && id.isEmpty();
  }

  public boolean hasComment() {
    return comment != null && !comment.isEmpty();
  }

  public boolean hasData() {
    return data != null && !data.isEmpty();
  }

  public boolean hasEvent() {
    return event != null && !event.isEmpty();
  }

  public boolean hasId() {
    return id != null && !id.isEmpty();
  }

  public boolean hasRetry() {
    return retry > 0;
  }
}
