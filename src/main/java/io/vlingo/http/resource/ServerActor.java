// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Logger;
import io.vlingo.actors.Returns;
import io.vlingo.actors.World;
import io.vlingo.common.BasicCompletes;
import io.vlingo.common.Completes;
import io.vlingo.common.Scheduled;
import io.vlingo.common.completes.SinkAndSourceBasedCompletes;
import io.vlingo.common.pool.ElasticResourcePool;
import io.vlingo.http.Context;
import io.vlingo.http.Filters;
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
import io.vlingo.wire.message.ConsumerByteBufferPool;

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
  private final ConsumerByteBufferPool responseBufferPool;
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
          requestResponseHttpContext.httpContext.completes.with(Response.of(Response.Status.BadRequest, "Missing content with timeout."));
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

//  private static final AtomicLong nextInstanceId = new AtomicLong(0);
//
//  private int fullCount = 0;
//  private int missingCount = 0;
//  private final long instanceId = nextInstanceId.incrementAndGet();

  private class ServerRequestChannelConsumer implements RequestChannelConsumer {
    private final Dispatcher dispatcher;

    ServerRequestChannelConsumer(final Dispatcher dispatcher) {
      this.dispatcher = dispatcher;
    }

    @Override
    public void closeWith(final RequestResponseContext<?> requestResponseContext, final Object data) {
//      logger().debug("===================== CLOSE WITH: " + data);
      if (data != null) {
        final Request request = filters.process((Request) data);
        final Completes<Response> completes = responseCompletes.of(requestResponseContext, /*request,*/ false, request.headers.headerOf(RequestHeader.XCorrelationID), true);
        final Context context = new Context(requestResponseContext, request, world.completesFor(Returns.value(completes)));
        dispatcher.dispatchFor(context);
      }
    }

    @Override
    public void consume(final RequestResponseContext<?> requestResponseContext, final ConsumerByteBuffer buffer) {
      boolean missingContent = false;
      Request unfilteredRequest = null;

      try {
        final RequestParser parser;
        boolean wasIncompleteContent = false;

        if (!requestResponseContext.hasConsumerData()) {
          parser = RequestParser.parserFor(buffer.asByteBuffer());
          requestResponseContext.consumerData(parser);
        } else {
          parser = requestResponseContext.consumerData();
          wasIncompleteContent = parser.isMissingContent();
//          logger().debug("============== (" + instanceId + ") WAS MISSING CONTENT FOR (" + (missingCount) + "): " + wasIncompleteContent);
//          if (wasIncompleteContent) {
//            logger().debug(
//                    parser.currentRequestText() +
//                    "\nNOW CONSUMING:\n" +
//                    Converters.bytesToText(buffer.array(), 0, buffer.limit()));
//          }
          parser.parseNext(buffer.asByteBuffer());
        }

        Context context = null;

        while (parser.hasFullRequest()) {
          unfilteredRequest = parser.fullRequest();
//          logger().debug("==============(" + instanceId + ") FULL REQUEST (" + (++fullCount) + "): \n" + unfilteredRequest);
          final boolean keepAlive = determineKeepAlive(requestResponseContext, unfilteredRequest);
          final Request request = filters.process(unfilteredRequest);
          final Completes<Response> completes = responseCompletes.of(requestResponseContext, /*unfilteredRequest,*/ false, request.headers.headerOf(RequestHeader.XCorrelationID), keepAlive);
          context = new Context(requestResponseContext, request, world.completesFor(Returns.value(completes)));
          dispatcher.dispatchFor(context);
          if (wasIncompleteContent) {
            requestsMissingContent.remove(requestResponseContext.id());
          }
        }

        if (parser.isMissingContent() && !requestsMissingContent.containsKey(requestResponseContext.id())) {
//          logger().debug("==============(" + instanceId + ") MISSING REQUEST CONTENT FOR (" + (++missingCount) + "): \n" + parser.currentRequestText());
          missingContent = true;
          if (context == null) {
            final Completes<Response> completes = responseCompletes.of(requestResponseContext, /*unfilteredRequest,*/ true, null, true);
            context = new Context(world.completesFor(Returns.value(completes)));
          }
          requestsMissingContent.put(requestResponseContext.id(), new RequestResponseHttpContext(requestResponseContext, context));
        }

      } catch (Exception e) {
//        logger().debug("=====================(" + instanceId + ") BAD REQUEST (1): " + unfilteredRequest);
//        final String requestContentText = Converters.bytesToText(buffer.array(), 0, buffer.limit());
//        logger().debug("=====================(" + instanceId + ") BAD REQUEST (2): " + requestContentText);
        logger().error("Request parsing failed.", e);
        responseCompletes.of(requestResponseContext, /*unfilteredRequest,*/ missingContent, null, false).with(Response.of(Response.Status.BadRequest, e.getMessage()));
      } finally {
        buffer.release();
      }
    }

    private boolean determineKeepAlive(final RequestResponseContext<?> requestResponseContext, final Request unfilteredRequest) {
      final boolean keepAlive = unfilteredRequest.headerMatches(RequestHeader.Connection, Header.ValueKeepAlive);

//      if (keepAlive) {
//        logger().debug("///////// SERVER REQUEST KEEP ALIVE /////////(" + instanceId + ")");
//      } else {
//        logger().debug("///////// SERVER REQUEST EAGER CLOSE /////////(" + instanceId + ")");
//      }

      return keepAlive;
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
    public Completes<Response> of(final RequestResponseContext<?> requestResponseContext, /*final Request request,*/ final boolean missingContent, final Header correlationId, final boolean keepAlive) {
      if (SinkAndSourceBasedCompletes.isToggleActive()) {
        return new SinkBasedBasedResponseCompletes(requestResponseContext, /*request,*/ missingContent, correlationId, keepAlive);
      } else {
        return new BasicCompletedBasedResponseCompletes(requestResponseContext, /*request,*/ missingContent, correlationId, keepAlive);
      }

    }
  }

  private class BasicCompletedBasedResponseCompletes extends BasicCompletes<Response> {
    final Header correlationId;
    final boolean keepAlive;
    final boolean missingContent;
//    final Request request;
    final RequestResponseContext<?> requestResponseContext;

    BasicCompletedBasedResponseCompletes(final RequestResponseContext<?> requestResponseContext, /*final Request request,*/ final boolean missingContent, final Header correlationId, final boolean keepAlive) {
      super(stage().scheduler());
      this.requestResponseContext = requestResponseContext;
//      this.request = request;
      this.missingContent = missingContent;
      this.correlationId = correlationId;
      this.keepAlive = keepAlive;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <O> Completes<O> with(final O response) {
      final Response unfilteredResponse = (Response) response;
      final Response filtered = filters.process(unfilteredResponse);
      final ConsumerByteBuffer buffer = bufferFor(filtered);
      final Response completedResponse = filtered.include(correlationId);
      requestResponseContext.respondWith(completedResponse.into(buffer), closeAfterResponse(unfilteredResponse));
      return (Completes<O>) this;
    }

    private ConsumerByteBuffer bufferFor(final Response response) {
      final int size = response.size();
      if (size < maxMessageSize) {
        return responseBufferPool.acquire("ServerActor#BasicCompletedBasedResponseCompletes#bufferFor");
      }

      return BasicConsumerByteBuffer.allocate(0, size + 1024);
    }

    private boolean closeAfterResponse(final Response response) {
      if (missingContent) return false;

      final char statusCategory = response.statusCode.charAt(0);
      if (statusCategory == '4' || statusCategory == '5') {
//        logger().debug(
//                "///////// SERVER RESPONSE CLOSED FOLLOWING ///////// KEEP-ALIVE: " + keepAlive +
//                "\n///////// REQUEST\n" + request +
//                "\n///////// RESPONSE\n" + response);
        return keepAlive;
      }

      final boolean keepAliveAfterResponse = keepAlive || response.headerMatches(RequestHeader.Connection, Header.ValueKeepAlive);

//      if (keepAliveAfterResponse) {
//        logger().debug("///////// SERVER RESPONSE KEEP ALIVE ////////");
//      } else {
//        logger().debug("///////// SERVER RESPONSE CLOSED FOLLOWING /////////\n" + response);
//      }

      return !keepAliveAfterResponse;
    }
  }

  private class SinkBasedBasedResponseCompletes extends SinkAndSourceBasedCompletes<Response> {
    final Header correlationId;
    final boolean keepAlive;
    final boolean missingContent;
//    final Request request;
    final RequestResponseContext<?> requestResponseContext;

    SinkBasedBasedResponseCompletes(final RequestResponseContext<?> requestResponseContext, /* final Request request,*/ final boolean missingContent, final Header correlationId, final boolean keepAlive) {
      super(stage().scheduler());
      this.requestResponseContext = requestResponseContext;
//      this.request = request;
      this.missingContent = missingContent;
      this.correlationId = correlationId;
      this.keepAlive = keepAlive;
    }

    @Override
    public <O> Completes<O> with(final O response) {
      final Response unfilteredResponse = (Response) response;
      final Response filtered = filters.process(unfilteredResponse);
      final ConsumerByteBuffer buffer = bufferFor(filtered);
      final Response completedResponse = filtered.include(correlationId);
      requestResponseContext.respondWith(completedResponse.into(buffer), closeAfterResponse(unfilteredResponse));
      return super.with(response);
    }

    private ConsumerByteBuffer bufferFor(final Response response) {
      final int size = response.size();
      if (size < maxMessageSize) {
        return responseBufferPool.acquire("ServerActor#SinkBasedBasedResponseCompletes#bufferFor");
      }

      return BasicConsumerByteBuffer.allocate(0, size + 1024);
    }

    private boolean closeAfterResponse(final Response response) {
      if (missingContent) return false;

      final char statusCategory = response.statusCode.charAt(0);
      if (statusCategory == '4' || statusCategory == '5') {
//        logger().debug(
//                "///////// SERVER RESPONSE CLOSED FOLLOWING ///////// KEEP-ALIVE: " + keepAlive +
//                "\n///////// REQUEST\n" + request +
//                "\n///////// RESPONSE\n" + response);
        return keepAlive;
      }

      final boolean keepAliveAfterResponse = keepAlive || response.headerMatches(RequestHeader.Connection, Header.ValueKeepAlive);

//      if (keepAliveAfterResponse) {
//        logger().debug("///////// SERVER RESPONSE KEEP ALIVE ////////");
//      } else {
//        logger().debug("///////// SERVER RESPONSE CLOSED FOLLOWING /////////\n" + response);
//      }

      return !keepAliveAfterResponse;
    }
  }
}
