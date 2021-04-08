// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource.sse;

import static io.vlingo.xoom.http.Response.Status.Ok;

import java.util.Arrays;
import java.util.Collection;

import io.vlingo.xoom.http.Header.Headers;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.ResponseHeader;
import io.vlingo.xoom.http.resource.Configuration;
import io.vlingo.xoom.wire.channel.RequestResponseContext;
import io.vlingo.xoom.wire.message.BasicConsumerByteBuffer;
import io.vlingo.xoom.wire.message.ConsumerByteBuffer;

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

  public SseClient(final RequestResponseContext<?> context, final Headers<ResponseHeader> extraHeaders) {
    this.context = context;
    headers.and(extraHeaders);
    this.builder = new StringBuilder();
    this.maxMessageSize = Configuration.instance.sizing().maxMessageSize;

    sendInitialResponse();
  }

  public SseClient(final RequestResponseContext<?> context) {
    this(context, Headers.empty());
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
    try {
      final Response response = Response.of(Ok, headers.copy());
      final ConsumerByteBuffer buffer = BasicConsumerByteBuffer.allocate(1, maxMessageSize);
      context.respondWith(response.into(buffer));
    } catch (Exception e) {
      // it's possible that I am being used for an unsubscribe
      // where the client has already disconnected and this
      // attempt will fail; ignore it and return.
    }
  }

  private String flatten(final Collection<SseEvent> events) {
    builder.delete(0, builder.length());

    for (final SseEvent event : events) {
      builder.append(event.sendable());
    }

    return builder.toString();
  }
}
