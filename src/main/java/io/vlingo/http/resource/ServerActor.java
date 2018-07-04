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
import io.vlingo.actors.BasicCompletes;
import io.vlingo.actors.Completes;
import io.vlingo.actors.Scheduled;
import io.vlingo.actors.World;
import io.vlingo.http.Context;
import io.vlingo.http.Header;
import io.vlingo.http.Request;
import io.vlingo.http.RequestHeader;
import io.vlingo.http.RequestParser;
import io.vlingo.http.Response;
import io.vlingo.wire.channel.RequestChannelConsumer;
import io.vlingo.wire.channel.RequestResponseContext;
import io.vlingo.wire.fdx.bidirectional.ServerRequestResponseChannel;
import io.vlingo.wire.message.ByteBufferPool;
import io.vlingo.wire.message.ConsumerByteBuffer;

public class ServerActor extends Actor implements Server, RequestChannelConsumer, Scheduled {
  private static final String ServerName = "vlingo-http-server";
  
  private final ServerRequestResponseChannel channel;
  private final Dispatcher[] dispatcherPool;
  private int dispatcherPoolIndex;
  private final Map<String,RequestResponseHttpContext> requestsMissingContent;
  private final long requestMissingContentTimeout;
  private final ByteBufferPool responseBufferPool;
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

    try {
      this.responseBufferPool = new ByteBufferPool(sizing.maxBufferPoolSize, sizing.maxMessageSize);

      this.dispatcherPool = new Dispatcher[sizing.dispatcherPoolSize];

      for (int idx = 0; idx < sizing.dispatcherPoolSize; ++idx) { 
        dispatcherPool[idx] = Dispatcher.startWith(stage(), resources);
      }

      this.channel =
              ServerRequestResponseChannel.start(
                      stage(),
                      selfAs(RequestChannelConsumer.class),
                      port,
                      "server-request-response-channel",
                      sizing.maxBufferPoolSize,
                      sizing.maxMessageSize,
                      timing.probeTimeout,
                      timing.probeInterval);

      logger().log("Server " + ServerName + " is listening on port: " + port);

      this.requestMissingContentTimeout = timing.requestMissingContentTimeout;
      
      stage().scheduler().schedule(selfAs(Scheduled.class), null, 1000L, requestMissingContentTimeout);

    } catch (Exception e) {
      final String message = "Failed to start server because: " + e.getMessage();
      logger().log(message, e);
      e.printStackTrace();
      throw new IllegalStateException(message);
    }
  }


  //=========================================
  // RequestChannelConsumer
  //=========================================

  @Override
  public void consume(final RequestResponseContext<?> requestResponseContext, final ConsumerByteBuffer buffer) {
    System.out.println("CONSUME");
    try {
      final RequestParser parser;

      if (!requestResponseContext.hasConsumerData()) {
        parser = RequestParser.parserFor(buffer.asByteBuffer());
        requestResponseContext.consumerData(parser);
      } else {
        parser = requestResponseContext.consumerData();
        parser.parseNext(buffer.asByteBuffer());
      }
      
      Context context = null;

      while (parser.hasFullRequest()) {
        final Request request = parser.fullRequest();
        final ResponseCompletes completes = new ResponseCompletes(requestResponseContext, request.headers.headerOf(RequestHeader.XCorrelationID));
        context = new Context(request, world.completesFor(completes));
        pooledDispatcher().dispatchFor(context);
      }

      if (parser.isMissingContent() && !requestsMissingContent.containsKey(requestResponseContext.id())) {
        requestsMissingContent.put(requestResponseContext.id(), new RequestResponseHttpContext(requestResponseContext, context));
      }

    } catch (Exception e) {
      e.printStackTrace();
      new ResponseCompletes(requestResponseContext, null).with(Response.of(Response.BadRequest + " " + e.getMessage()));
    } finally {
      buffer.release();
    }
  }


  //=========================================
  // Scheduled
  //=========================================

  @Override
  public void intervalSignal(final Scheduled scheduled, final Object data) {
    failTimedOutMissingContentRequests();
  }


  //=========================================
  // Stoppable
  //=========================================

  @Override
  public void stop() {
    logger().log("Server stopped.");

    failTimedOutMissingContentRequests();

    channel.stop();
    channel.close();

    for (final Dispatcher dispatcher : dispatcherPool) {
      dispatcher.stop();
    }

    super.stop();
  }


  //=========================================
  // internal implementation
  //=========================================

  private void failTimedOutMissingContentRequests() {
    if (requestsMissingContent.isEmpty()) return;

    final List<String> toRemove = new ArrayList<>(); // prevent ConcurrentModificationException

    for (final String id : requestsMissingContent.keySet()) {
      final RequestResponseHttpContext requestResponseHttpContext = requestsMissingContent.get(id);

      if (requestResponseHttpContext.requestResponseContext.hasConsumerData()) {
        final RequestParser parser = requestResponseHttpContext.requestResponseContext.consumerData();
        if (parser.hasMissingContentTimeExpired(requestMissingContentTimeout)) {
          toRemove.add(id);
          requestResponseHttpContext.httpContext.completes.with(Response.of(Response.BadRequest + " Missing content."));
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

    @Override
    @SuppressWarnings("unchecked")
    public <O> Completes<O> with(final O response) {
      requestResponseContext.respondWith(((Response) response).include(correlationId).into(responseBufferPool.accessFor("response")));
      return (Completes<O>) this;
    }
  }
}
