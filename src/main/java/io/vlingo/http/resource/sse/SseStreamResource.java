// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.sse;

import static io.vlingo.http.Response.Status.Ok;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Definition;
import io.vlingo.actors.World;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseHeader;
import io.vlingo.http.Header.Headers;
import io.vlingo.http.resource.Configuration;
import io.vlingo.http.resource.ResourceHandler;
import io.vlingo.wire.channel.RequestResponseContext;
import io.vlingo.wire.message.BasicConsumerByteBuffer;
import io.vlingo.wire.message.ConsumerByteBuffer;

public class SseStreamResource extends ResourceHandler {
  private static final ResponseHeader Connection;
  private static final ResponseHeader ContentType;
  private static final ResponseHeader TransferEncoding;
  private static final Headers<ResponseHeader> headers;

  static {
    Connection = ResponseHeader.of(ResponseHeader.Connection, "keep-alive");
    ContentType = ResponseHeader.of(ResponseHeader.ContentType, "text/event-stream");
    TransferEncoding = ResponseHeader.of(ResponseHeader.TransferEncoding, "chunked");

    headers = Headers.empty();
    headers.and(Connection).and(ContentType).and(TransferEncoding);
  }

  private final SseStream stream;

  public SseStreamResource(final World world) {
    this.stream = world.actorFor(Definition.has(SseStreamActor.class, Definition.NoParameters), SseStream.class);
  }

  public static class SseStreamActor extends Actor implements SseStream {
    private final ConsumerByteBuffer buffer;
    private final StringBuilder builder;
    private final Map<String,RequestResponseContext<?>> subscribers;

    public SseStreamActor() {
      this.buffer = BasicConsumerByteBuffer.allocate(1, Configuration.instance.sizing().maxMessageSize);
      this.builder = new StringBuilder();
      this.subscribers = new HashMap<>();
    }

    @Override
    public void publish(final SseEvent event) {
      for (final RequestResponseContext<?> context : subscribers.values()) {
        sendTo(context, event);
      }
    }

    @Override
    public void publish(final Collection<SseEvent> events) {
      for (final RequestResponseContext<?> context : subscribers.values()) {
        sendTo(context, events);
      }
    }

    @Override
    public void sendTo(final SseSubscriber subscriber, final SseEvent event) {
//      final RequestResponseContext<?> context = subscribers.get(name);
//      if (context != null) {
//        sendTo(context, event);
//      }
    }

    @Override
    public void sendTo(final SseSubscriber subscriber, final Collection<SseEvent> events) {
//      final RequestResponseContext<?> context = subscribers.get(name);
//      if (context != null) {
//        sendTo(context, events);
//      }
    }

    @Override
    public void subscribe(final SseSubscriber subscriber) {
//      subscribers.put(name, context);
    }

    @Override
    public void unsubscribe(final SseSubscriber subscriber) {
//      final RequestResponseContext<?> context = subscribers.remove(name);
//      context.abandon();
    }

    private String flatten(final Collection<SseEvent> events) {
      builder.delete(0, builder.length());

      for (final SseEvent event : events) {
        builder.append(event);
      }

      return builder.toString();
    }

    private void sendTo(final RequestResponseContext<?> context, final SseEvent event) {
      final Response response = Response.of(Ok, headers, event.sendable());
      context.respondWith(response.into(buffer));
    }

    private void sendTo(final RequestResponseContext<?> context, final Collection<SseEvent> events) {
      final Response response = Response.of(Ok, headers, flatten(events));
      context.respondWith(response.into(buffer));
    }
  }
}
