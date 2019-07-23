// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.sse;

import static io.vlingo.http.Response.Status.Ok;

import java.util.Arrays;
import java.util.Collection;

import io.vlingo.http.Header.Headers;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseHeader;
import io.vlingo.http.resource.Configuration;
import io.vlingo.wire.channel.RequestResponseContext;
import io.vlingo.wire.message.BasicConsumerByteBuffer;
import io.vlingo.wire.message.ConsumerByteBuffer;

public class SseClient {
  private static final ResponseHeader CacheControl;
  private static final ResponseHeader Connection;
  private static final ResponseHeader ContentType;
  private static final Headers<ResponseHeader> headers;

  static {
    CacheControl = ResponseHeader.of(ResponseHeader.CacheControl, "no-cache");
    Connection = ResponseHeader.of(ResponseHeader.Connection, "keep-alive");
    ContentType = ResponseHeader.of(ResponseHeader.ContentType, "text/event-stream;charset=utf-8");

    headers = Headers.empty();
    headers.and(Connection).and(ContentType).and(CacheControl);
  }

  private final StringBuilder builder;
  private final RequestResponseContext<?> context;
  private final int maxMessageSize;

  public SseClient(final RequestResponseContext<?> context) {
    this.context = context;
    this.builder = new StringBuilder();
    this.maxMessageSize = Configuration.instance.sizing().maxMessageSize;

    sendInitialResponse();
  }

  public void close() {
    context.abandon();
  }

  public String id() {
    return context.id();
  }

  public void send(final SseEvent event) {
    send(event.sendable());
  }

  public void send(final SseEvent... events) {
    send(Arrays.asList(events));
  }

  public void send(final Collection<SseEvent> events) {
    final String entity = flatten(events);
    send(entity);
  }

  private void send(final String entity) {
    final ConsumerByteBuffer buffer = BasicConsumerByteBuffer.allocate(1, maxMessageSize);
    context.respondWith(buffer.put(entity.getBytes()).flip());
  }

  private void sendInitialResponse() {
    final Response response = Response.of(Ok, headers.copy());
    final ConsumerByteBuffer buffer = BasicConsumerByteBuffer.allocate(1, maxMessageSize);
    context.respondWith(response.into(buffer));
  }

  private String flatten(final Collection<SseEvent> events) {
    builder.delete(0, builder.length());

    for (final SseEvent event : events) {
      builder.append(event.sendable());
    }

    return builder.toString();
  }
}
