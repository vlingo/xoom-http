// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vlingo.actors.Actor;
import io.vlingo.actors.World;
import io.vlingo.common.BasicCompletes;
import io.vlingo.common.Completes;
import io.vlingo.common.Scheduled;
import io.vlingo.http.Context;
import io.vlingo.http.Header;
import io.vlingo.http.Request;
import io.vlingo.http.RequestHeader;
import io.vlingo.http.RequestParser;
import io.vlingo.http.Response;
import io.vlingo.http.resource.Configuration.Sizing;
import io.vlingo.http.resource.Configuration.Timing;
import io.vlingo.wire.channel.RequestChannelConsumer;
import io.vlingo.wire.channel.RequestChannelConsumerProvider;
import io.vlingo.wire.channel.RequestResponseContext;
import io.vlingo.wire.fdx.bidirectional.ServerRequestResponseChannel;
import io.vlingo.wire.message.BasicConsumerByteBuffer;
import io.vlingo.wire.message.ConsumerByteBuffer;

public class ServerActor extends Actor implements Server, RequestChannelConsumerProvider, Scheduled<Object> {
  static final String ChannelName = "server-request-response-channel";
  static final String ServerName = "vlingo-http-server";

  private final ServerRequestResponseChannel channel;
  private final Dispatcher[] dispatcherPool;
  private int dispatcherPoolIndex;
  private final int maxMessageSize;
  private final Map<String,RequestResponseHttpContext> requestsMissingContent;
  private final long requestMissingContentTimeout;
  //private final ByteBufferPool responseBufferPool;
  private final World world;

  public ServerActor(
          final Resources resources,
          final int port,
          final Sizing sizing,
          final Timing timing)
  throws Exception {
    this.dispatcherPoolIndex = 0;
    this.world = stage().world();
    this.requestsMissingContent = new HashMap<>();
    this.maxMessageSize = sizing.maxMessageSize;

    try {
      //this.responseBufferPool = new ByteBufferPool(sizing.maxBufferPoolSize, sizing.maxMessageSize);

      this.dispatcherPool = new Dispatcher[sizing.dispatcherPoolSize];

      for (int idx = 0; idx < sizing.dispatcherPoolSize; ++idx) { 
        dispatcherPool[idx] = Dispatcher.startWith(stage(), resources);
      }

      this.channel =
              ServerRequestResponseChannel.start(
                      stage(),
                      stage().world().addressFactory().withHighId(ChannelName),
                      "queueMailbox",
                      this,
                      port,
                      ChannelName,
                      sizing.processorPoolSize,
                      sizing.maxBufferPoolSize,
                      sizing.maxMessageSize,
                      timing.probeInterval);

      logger().log("Server " + ServerName + " is listening on port: " + port);

      this.requestMissingContentTimeout = timing.requestMissingContentTimeout;

    } catch (Exception e) {
      final String message = "Failed to start server because: " + e.getMessage();
      logger().log(message, e);
      throw new IllegalStateException(message);
    }
  }

  //=========================================
  // Server
  //=========================================

  @Override
  public Completes<Boolean> shutDown() {
    stop();

    return completes().with(true);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Completes<Boolean> startUp() {
    stage().scheduler().schedule(selfAs(Scheduled.class), null, 1000L, requestMissingContentTimeout);

    return completes().with(true);
  }


  //=========================================
  // RequestChannelConsumerProvider
  //=========================================

  @Override
  public RequestChannelConsumer requestChannelConsumer() {
    return new ServerRequestChannelConsumer(pooledDispatcher());
  }


  //=========================================
  // Scheduled
  //=========================================

  @Override
  public void intervalSignal(final Scheduled<Object> scheduled, final Object data) {
    failTimedOutMissingContentRequests();
  }


  //=========================================
  // Stoppable
  //=========================================

  @Override
  public void stop() {
    logger().log("Server stopping...");

    failTimedOutMissingContentRequests();

    channel.stop();
    channel.close();

    for (final Dispatcher dispatcher : dispatcherPool) {
      dispatcher.stop();
    }

    logger().log("Server stopped.");

    super.stop();
  }


  //=========================================
  // internal implementation
  //=========================================

  private void failTimedOutMissingContentRequests() {
    if (isStopped()) return;
    if (requestsMissingContent.isEmpty()) return;

    final List<String> toRemove = new ArrayList<>(); // prevent ConcurrentModificationException

    for (final String id : requestsMissingContent.keySet()) {
      final RequestResponseHttpContext requestResponseHttpContext = requestsMissingContent.get(id);

      if (requestResponseHttpContext.requestResponseContext.hasConsumerData()) {
        final RequestParser parser = requestResponseHttpContext.requestResponseContext.consumerData();
        if (parser.hasMissingContentTimeExpired(requestMissingContentTimeout)) {
          requestResponseHttpContext.requestResponseContext.consumerData(null);
          toRemove.add(id);
          requestResponseHttpContext.httpContext.completes.with(Response.of(Response.Status.BadRequest, "Missing content."));
          requestResponseHttpContext.requestResponseContext.consumerData(null);
        }
      } else {
        toRemove.add(id); // already closed?
      }
    }

    for (final String id : toRemove) {
      requestsMissingContent.remove(id);
    }
  }

  private Dispatcher pooledDispatcher() {
    if (dispatcherPoolIndex >= dispatcherPool.length) {
      dispatcherPoolIndex = 0;
    }
    return dispatcherPool[dispatcherPoolIndex++];
  }


  //=========================================
  // RequestChannelConsumer
  //=========================================

  private class ServerRequestChannelConsumer implements RequestChannelConsumer {
    private final Dispatcher dispatcher;

    ServerRequestChannelConsumer(final Dispatcher dispatcher) {
      this.dispatcher = dispatcher;
    }

    @Override
    public void closeWith(final RequestResponseContext<?> requestResponseContext, final Object data) {
      if (data != null) {
        final Request request = (Request) data;
        final ResponseCompletes completes = new ResponseCompletes(requestResponseContext, request.headers.headerOf(RequestHeader.XCorrelationID));
        final Context context = new Context(requestResponseContext, request, world.completesFor(completes));
        dispatcher.dispatchFor(context);
      }
    }

    @Override
    public void consume(final RequestResponseContext<?> requestResponseContext, final ConsumerByteBuffer buffer) {
      try {
        final RequestParser parser;
        boolean wasIncompleteContent = false;

        if (!requestResponseContext.hasConsumerData()) {
          parser = RequestParser.parserFor(buffer.asByteBuffer());
          requestResponseContext.consumerData(parser);
        } else {
          parser = requestResponseContext.consumerData();
          wasIncompleteContent = parser.isMissingContent();
          parser.parseNext(buffer.asByteBuffer());
        }

        Context context = null;

        while (parser.hasFullRequest()) {
          final Request request = parser.fullRequest();
          final ResponseCompletes completes = new ResponseCompletes(requestResponseContext, request.headers.headerOf(RequestHeader.XCorrelationID));
          context = new Context(requestResponseContext, request, world.completesFor(completes));
          dispatcher.dispatchFor(context);
          if (wasIncompleteContent) {
            requestsMissingContent.remove(requestResponseContext.id());
          }
        }

        if (parser.isMissingContent() && !requestsMissingContent.containsKey(requestResponseContext.id())) {
          if (context == null) {
            final ResponseCompletes completes = new ResponseCompletes(requestResponseContext);
            context = new Context(world.completesFor(completes));
          }
          requestsMissingContent.put(requestResponseContext.id(), new RequestResponseHttpContext(requestResponseContext, context));
        }

      } catch (Exception e) {
        logger().log("Request parsing failed.", e);
        new ResponseCompletes(requestResponseContext, null).with(Response.of(Response.Status.BadRequest, e.getMessage()));
      } finally {
        buffer.release();
      }
    }
  }

  //=========================================
  // RequestResponseHttpContext
  //=========================================

  private class RequestResponseHttpContext {
    final Context httpContext;
    final RequestResponseContext<?> requestResponseContext;
    
    RequestResponseHttpContext(final RequestResponseContext<?> requestResponseContext, final Context httpContext) {
      this.requestResponseContext = requestResponseContext;
      this.httpContext = httpContext;
    }
  }

  //=========================================
  // ResponseCompletes
  //=========================================

  private class ResponseCompletes extends BasicCompletes<Response> {
    final Header correlationId;
    final RequestResponseContext<?> requestResponseContext;

    ResponseCompletes(final RequestResponseContext<?> requestResponseContext, final Header correlationId) {
      super(stage().scheduler());
      this.requestResponseContext = requestResponseContext;
      this.correlationId = correlationId;
    }

    ResponseCompletes(final RequestResponseContext<?> requestResponseContext) {
      this(requestResponseContext, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <O> Completes<O> with(final O response) {
      final ConsumerByteBuffer buffer = BasicConsumerByteBuffer.allocate(0, maxMessageSize);
      requestResponseContext.respondWith(((Response) response).include(correlationId).into(buffer));
      return (Completes<O>) this;
    }
  }
}
