// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Logger;
import io.vlingo.actors.Returns;
import io.vlingo.actors.World;
import io.vlingo.common.BasicCompletes;
import io.vlingo.common.Completes;
import io.vlingo.common.Scheduled;
import io.vlingo.common.completes.SinkAndSourceBasedCompletes;
import io.vlingo.common.pool.ElasticResourcePool;
import io.vlingo.common.pool.ResourcePool;
import io.vlingo.http.*;
import io.vlingo.http.resource.Configuration.Sizing;
import io.vlingo.http.resource.Configuration.Timing;
import io.vlingo.wire.channel.RequestChannelConsumer;
import io.vlingo.wire.channel.RequestChannelConsumerProvider;
import io.vlingo.wire.channel.RequestResponseContext;
import io.vlingo.wire.fdx.bidirectional.ServerRequestResponseChannel;
import io.vlingo.wire.message.BasicConsumerByteBuffer;
import io.vlingo.wire.message.ConsumerByteBuffer;
import io.vlingo.wire.message.ConsumerByteBufferPool;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerActor extends Actor implements Server, RequestChannelConsumerProvider, Scheduled<Object> {
  static final String ChannelName = "server-request-response-channel";
  static final String ServerName = "vlingo-http-server";

  private final ServerRequestResponseChannel channel;
  private final Dispatcher[] dispatcherPool;
  private int dispatcherPoolIndex;
  private final Filters filters;
  private final int maxMessageSize;
  private final Map<String,RequestResponseHttpContext> requestsMissingContent;
  private final long requestMissingContentTimeout;
  private final ResourcePool<ConsumerByteBuffer, Void> responseBufferPool;
  private final World world;

  public ServerActor(
          final Resources resources,
          final Filters filters,
          final int port,
          final Sizing sizing,
          final Timing timing,
          final String channelMailboxTypeName)
  throws Exception {
    final long start = Instant.now().toEpochMilli();

    this.filters = filters;
    this.dispatcherPoolIndex = 0;
    this.world = stage().world();
    this.requestsMissingContent = new HashMap<>();
    this.maxMessageSize = sizing.maxMessageSize;

    try {
      responseBufferPool = new ConsumerByteBufferPool(
        ElasticResourcePool.Config.of(sizing.maxBufferPoolSize), sizing.maxMessageSize);

      this.dispatcherPool = new Dispatcher[sizing.dispatcherPoolSize];

      for (int idx = 0; idx < sizing.dispatcherPoolSize; ++idx) {
        dispatcherPool[idx] = Dispatcher.startWith(stage(), resources);
      }

      this.channel =
              ServerRequestResponseChannel.start(
                      stage(),
                      stage().world().addressFactory().withHighId(ChannelName),
                      channelMailboxTypeName,
                      this,
                      port,
                      ChannelName,
                      sizing.processorPoolSize,
                      sizing.maxBufferPoolSize,
                      sizing.maxMessageSize,
                      timing.probeInterval,
                      timing.probeTimeout);

      final long end = Instant.now().toEpochMilli();

      logger().info("Server " + ServerName + " is listening on port: " + port + " started in " + (end - start) + " ms");

      this.requestMissingContentTimeout = timing.requestMissingContentTimeout;

      logResourceMappings(resources);

    } catch (Exception e) {
      final String message = "Failed to start server because: " + e.getMessage();
      logger().error(message, e);
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
    logger().info("Server stopping...");

    failTimedOutMissingContentRequests();

    channel.stop();
    channel.close();

    for (final Dispatcher dispatcher : dispatcherPool) {
      dispatcher.stop();
    }

    filters.stop();

    logger().info("Server stopped.");

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

  private void logResourceMappings(final Resources resources) {
    final Logger logger = logger();
    for (final String resourceName : resources.namedResources.keySet()) {
      resources.namedResources.get(resourceName).log(logger);
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
        final Request request = filters.process((Request) data);
        final Completes<Response> completes = responseCompletes.of(requestResponseContext, request.headers.headerOf(RequestHeader.XCorrelationID));
        final Context context = new Context(requestResponseContext, request, world.completesFor(Returns.value(completes)));
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
          final Request request = filters.process(parser.fullRequest());
          final Completes<Response> completes = responseCompletes.of(requestResponseContext, request.headers.headerOf(RequestHeader.XCorrelationID));
          context = new Context(requestResponseContext, request, world.completesFor(Returns.value(completes)));
          dispatcher.dispatchFor(context);
          if (wasIncompleteContent) {
            requestsMissingContent.remove(requestResponseContext.id());
          }
        }

        if (parser.isMissingContent() && !requestsMissingContent.containsKey(requestResponseContext.id())) {
          if (context == null) {
            final Completes<Response> completes = responseCompletes.of(requestResponseContext, null);
            context = new Context(world.completesFor(Returns.value(completes)));
          }
          requestsMissingContent.put(requestResponseContext.id(), new RequestResponseHttpContext(requestResponseContext, context));
        }

      } catch (Exception e) {
        logger().error("Request parsing failed.", e);
        responseCompletes.of(requestResponseContext, null).with(Response.of(Response.Status.BadRequest, e.getMessage()));
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

  ResponseCompletes responseCompletes = new ResponseCompletes();
  private class ResponseCompletes {
    public Completes<Response> of(final RequestResponseContext<?> requestResponseContext, final Header correlationId) {
      if (SinkAndSourceBasedCompletes.isToggleActive()) {
        return new SinkBasedBasedResponseCompletes(requestResponseContext, correlationId);
      } else {
        return new BasicCompletedBasedResponseCompletes(requestResponseContext, correlationId);
      }

    }
  }

  private class BasicCompletedBasedResponseCompletes extends BasicCompletes<Response> {
    final Header correlationId;
    final RequestResponseContext<?> requestResponseContext;

    BasicCompletedBasedResponseCompletes(final RequestResponseContext<?> requestResponseContext, final Header correlationId) {
      super(stage().scheduler());
      this.requestResponseContext = requestResponseContext;
      this.correlationId = correlationId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <O> Completes<O> with(final O response) {
      final Response filtered = filters.process((Response) response);
      final ConsumerByteBuffer buffer = bufferFor(filtered);
      final Response completedResponse = filtered.include(correlationId);
      requestResponseContext.respondWith(completedResponse.into(buffer));
      return (Completes<O>) this;
    }

    private ConsumerByteBuffer bufferFor(final Response response) {
      final int size = response.size();
      if (size < maxMessageSize) {
        return responseBufferPool.acquire();
      }

      return BasicConsumerByteBuffer.allocate(0, size + 1024);
    }
  }

  private class SinkBasedBasedResponseCompletes extends SinkAndSourceBasedCompletes<Response> {
    final Header correlationId;
    final RequestResponseContext<?> requestResponseContext;

    SinkBasedBasedResponseCompletes(final RequestResponseContext<?> requestResponseContext, final Header correlationId) {
      super(stage().scheduler());
      this.requestResponseContext = requestResponseContext;
      this.correlationId = correlationId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <O> Completes<O> with(final O response) {
      final Response filtered = filters.process((Response) response);
      final ConsumerByteBuffer buffer = bufferFor(filtered);
      final Response completedResponse = filtered.include(correlationId);
      requestResponseContext.respondWith(completedResponse.into(buffer));
      return super.with(response);
    }

    private ConsumerByteBuffer bufferFor(final Response response) {
      final int size = response.size();
      if (size < maxMessageSize) {
        return responseBufferPool.acquire();
      }

      return BasicConsumerByteBuffer.allocate(0, size + 1024);
    }
  }
}
