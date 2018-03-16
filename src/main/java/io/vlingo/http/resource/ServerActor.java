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
import io.vlingo.actors.Cancellable;
import io.vlingo.actors.Completes;
import io.vlingo.actors.Scheduled;
import io.vlingo.actors.World;
import io.vlingo.http.Context;
import io.vlingo.http.Request;
import io.vlingo.http.RequestParser;
import io.vlingo.http.Response;
import io.vlingo.wire.channel.RequestChannelConsumer;
import io.vlingo.wire.channel.RequestResponseContext;
import io.vlingo.wire.channel.ResponseData;
import io.vlingo.wire.fdx.bidirectional.ServerRequestResponseChannel;

public class ServerActor extends Actor implements Server, RequestChannelConsumer {
  private static final String ServerName = "vlingo-http-server";
  
  private final Cancellable cancellable;
  private final ServerRequestResponseChannel channel;
  private final Dispatcher[] dispatcherPool;
  private int dispatcherPoolIndex;
  private Map<String,RequestResponseHttpContext> requestsMissingContent;
  private final long requestMissingContentTimeout;
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
      this.dispatcherPool = new Dispatcher[sizing.dispatcherPoolSize];

      for (int idx = 0; idx < sizing.dispatcherPoolSize; ++idx) { 
        dispatcherPool[idx] = Dispatcher.startWith(stage(), resources);
      }

      this.channel = new ServerRequestResponseChannel(port, ServerName, sizing.maxBufferPoolSize, sizing.maxMessageSize, timing.probeTimeout, logger());

      channel.openFor(selfAs(RequestChannelConsumer.class));

      logger().log("Server " + ServerName + " is listening on port: " + port);

      cancellable = stage().scheduler().schedule(selfAs(Scheduled.class), null, 0, timing.probeInterval);
      
      this.requestMissingContentTimeout = timing.requestMissingContentTimeout;

    } catch (Exception e) {
      final String message = "Failed to start server because: " + e.getMessage();
      logger().log(message, e);
      throw new IllegalStateException(message);
    }
  }


  //=========================================
  // RequestChannelConsumer
  //=========================================

  @Override
  public void consume(final RequestResponseContext<?> requestResponseContext) {
    try {
      final RequestParser parser;

      if (!requestResponseContext.hasConsumerData()) {
        parser = RequestParser.parserFor(requestResponseContext.requestBuffer().asByteBuffer());
        requestResponseContext.consumerData(parser);
      } else {
        parser = requestResponseContext.consumerData();
        parser.parseNext(requestResponseContext.requestBuffer().asByteBuffer());
      }

      Context context = null;

      while (parser.hasFullRequest()) {
        final Request request = parser.fullRequest();
        context = new Context(request, world.completesFor(new ResponseCompletes(requestResponseContext)));

        pooledDispatcher().dispatchFor(context);
      }

      if (parser.hasCompleted()) {
        requestResponseContext.consumerData(null);
      }

      if (parser.isMissingContent() && !requestsMissingContent.containsKey(requestResponseContext.id())) {
        requestsMissingContent.put(requestResponseContext.id(), new RequestResponseHttpContext(requestResponseContext, context));
      }

    } catch (Exception e) {
      new ResponseCompletes(requestResponseContext).with(Response.of(Response.BadRequest + " " + e.getMessage()));
    }
  }


  //=========================================
  // Scheduled
  //=========================================

  @Override
  public void intervalSignal(final Scheduled scheduled, final Object data) {
    channel.probeChannel();

    failTimedOutMissingContentRequests();
  }


  //=========================================
  // Stoppable
  //=========================================

  @Override
  public void stop() {
    failTimedOutMissingContentRequests();

    cancellable.cancel();
    channel.close();

    for (final Dispatcher dispatcher : dispatcherPool) {
      dispatcher.stop();
    }
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

  private class ResponseCompletes implements Completes<Response> {
    final RequestResponseContext<?> requestResponseContext;

    ResponseCompletes(final RequestResponseContext<?> requestResponseContext) {
      this.requestResponseContext = requestResponseContext;
    }

    @Override
    public void with(final Response response) {
      final ResponseData responseData = requestResponseContext.responseData();
      requestResponseContext.respondWith(response.into(responseData.buffer));
    }
  }
}
