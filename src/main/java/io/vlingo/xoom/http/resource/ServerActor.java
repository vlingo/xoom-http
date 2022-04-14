// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import static io.vlingo.xoom.http.RequestHeader.XForwardedFor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.Logger;
import io.vlingo.xoom.actors.Returns;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.common.Scheduled;
import io.vlingo.xoom.common.completes.FutureCompletes;
import io.vlingo.xoom.common.pool.ElasticResourcePool;
import io.vlingo.xoom.http.Context;
import io.vlingo.xoom.http.Filters;
import io.vlingo.xoom.http.Header;
import io.vlingo.xoom.http.Request;
import io.vlingo.xoom.http.RequestHeader;
import io.vlingo.xoom.http.RequestParser;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.resource.Configuration.Sizing;
import io.vlingo.xoom.http.resource.Configuration.Timing;
import io.vlingo.xoom.http.resource.DispatcherPool.AbstractDispatcherPool;
import io.vlingo.xoom.http.resource.agent.AgentDispatcherPool;
import io.vlingo.xoom.http.resource.agent.HttpAgent;
import io.vlingo.xoom.http.resource.agent.HttpRequestChannelConsumer;
import io.vlingo.xoom.http.resource.agent.HttpRequestChannelConsumerProvider;
import io.vlingo.xoom.wire.channel.RequestChannelConsumer;
import io.vlingo.xoom.wire.channel.RequestResponseContext;
import io.vlingo.xoom.wire.fdx.bidirectional.ServerRequestResponseChannel;
import io.vlingo.xoom.wire.message.BasicConsumerByteBuffer;
import io.vlingo.xoom.wire.message.ConsumerByteBuffer;
import io.vlingo.xoom.wire.message.ConsumerByteBufferPool;

public class ServerActor extends Actor implements Server, HttpRequestChannelConsumerProvider, Scheduled<Object> {
  static final String ChannelName = "server-request-response-channel";
  static final String ServerName = "xoom-http-server";

  private final HttpAgent agent;
  private final ServerRequestResponseChannel channel;
  private final DispatcherPool dispatcherPool;
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
          final int dispatcherPoolSize)
  throws Exception {
    final long start = Instant.now().toEpochMilli();

    this.agent = HttpAgent.initialize(this, port, false, dispatcherPoolSize, logger());

    this.channel = null;                            // unused
    this.filters = filters;
    this.world = stage().world();
    this.dispatcherPool = new AgentDispatcherPool(stage(), resources, dispatcherPoolSize);
    this.requestsMissingContent = new HashMap<>();  // unused
    this.maxMessageSize = 0;                        // unused
    this.responseBufferPool = null;                 // unused
    this.requestMissingContentTimeout = -1;         // unused

    final long end = Instant.now().toEpochMilli();

    logger().info("Server " + ServerName + " is listening on port: " + port + " started in " + (end - start) + " ms");

    logResourceMappings(resources);
  }

  public ServerActor(
          final Resources resources,
          final Filters filters,
          final int port,
          final Sizing sizing,
          final Timing timing,
          final String channelMailboxTypeName)
  throws Exception {
    final long start = Instant.now().toEpochMilli();

    this.agent = null;                              // unused
    this.filters = filters;
    this.world = stage().world();
    this.requestsMissingContent = new HashMap<>();
    this.maxMessageSize = sizing.maxMessageSize;

    try {
      responseBufferPool = new ConsumerByteBufferPool(
        ElasticResourcePool.Config.of(sizing.maxBufferPoolSize), sizing.maxMessageSize);

      this.dispatcherPool = new ServerDispatcherPool(stage(), resources, sizing.dispatcherPoolSize);

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
    if (requestMissingContentTimeout > 0) {
      stage().scheduler().schedule(selfAs(Scheduled.class), null, 1000L, requestMissingContentTimeout);
    }

    return completes().with(true);
  }


  //=========================================
  // RequestChannelConsumerProvider
  //=========================================

  @Override
  public RequestChannelConsumer requestChannelConsumer() {
    return httpRequestChannelConsumer();
  }

  @Override
  public HttpRequestChannelConsumer httpRequestChannelConsumer() {
    return new ServerRequestChannelConsumer(dispatcherPool.dispatcher());
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

    if (agent != null) {
      agent.close();
    } else {
      failTimedOutMissingContentRequests();

      channel.stop();
      channel.close();

      dispatcherPool.close();

      filters.stop();
    }

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


  private static class ServerDispatcherPool extends AbstractDispatcherPool {
    private AtomicLong dispatcherPoolIndex;
    private int dispatcherPoolSize;

    ServerDispatcherPool(final Stage stage, final Resources resources, final int dispatcherPoolSize) {
      super(stage, resources, dispatcherPoolSize);

      this.dispatcherPoolIndex = new AtomicLong(0);
      this.dispatcherPoolSize = dispatcherPool.length;
    }

    @Override
    public Dispatcher dispatcher() {
      final int index = (int) (dispatcherPoolIndex.incrementAndGet() % dispatcherPoolSize);

      return dispatcherPool[index];
    }
  }

  //=========================================
  // RequestChannelConsumer
  //=========================================

//  private static final AtomicLong nextInstanceId = new AtomicLong(0);
//
//  private int fullCount = 0;
//  private int missingCount = 0;
//  private final long instanceId = nextInstanceId.incrementAndGet();

  private class ServerRequestChannelConsumer implements HttpRequestChannelConsumer {
    private final Dispatcher dispatcher;

    ServerRequestChannelConsumer(final Dispatcher dispatcher) {
      this.dispatcher = dispatcher;
    }

    @Override
    public void closeWith(final RequestResponseContext<?> requestResponseContext, final Object data) {
//    logger().debug("===================== CLOSE WITH: " + data);
      if (data != null) {
        final Request request = filters.process((Request) data);
        final Completes<Response> completes = responseCompletes.of(requestResponseContext, request, false, request.headers.headerOf(RequestHeader.XCorrelationID), true);
        final Context context = new Context(requestResponseContext, request, world.completesFor(Returns.value(completes)));
        dispatcher.dispatchFor(context);
      }
    }

    @Override
    public void consume(final RequestResponseContext<?> requestResponseContext, final ConsumerByteBuffer buffer) {
      boolean wasIncompleteContent = false;
      boolean missingContent = false;

      try {
        final RequestParser parser;

        if (!requestResponseContext.hasConsumerData()) {
          parser = RequestParser.parserFor(buffer.asByteBuffer());
          requestResponseContext.consumerData(parser);
        } else {
          parser = requestResponseContext.consumerData();
          wasIncompleteContent = parser.isMissingContent();
//        logger().debug("============== (" + instanceId + ") WAS MISSING CONTENT FOR (" + (missingCount) + "): " + wasIncompleteContent);
//        if (wasIncompleteContent) {
//          logger().debug(
//                  parser.currentRequestText() +
//                  "\nNOW CONSUMING:\n" +
//                  Converters.bytesToText(buffer.array(), 0, buffer.limit()));
//        }
          parser.parseNext(buffer.asByteBuffer());
        }

        Context context = null;

        while (parser.hasFullRequest()) {
          final Request enrichedRequest = enrichRequest(requestResponseContext, parser.fullRequest());
          context = consume(requestResponseContext, enrichedRequest, wasIncompleteContent);
        }

        if (parser.isMissingContent() && !requestsMissingContent.containsKey(requestResponseContext.id())) {
//        logger().debug("==============(" + instanceId + ") MISSING REQUEST CONTENT FOR (" + (++missingCount) + "): \n" + parser.currentRequestText());
          missingContent = true;
          if (context == null) {
            final Completes<Response> completes = responseCompletes.of(requestResponseContext.typed(), null, true, null, true);
            context = new Context(world.completesFor(Returns.value(completes)));
          }
          requestsMissingContent.put(requestResponseContext.id(), new RequestResponseHttpContext(requestResponseContext, context));
        }

      } catch (Exception e) {
//      logger().debug("=====================(" + instanceId + ") BAD REQUEST (1): " + unfilteredRequest);
//      final String requestContentText = Converters.bytesToText(buffer.array(), 0, buffer.limit());
//      logger().debug("=====================(" + instanceId + ") BAD REQUEST (2): " + requestContentText);
        logger().error("Request parsing failed.", e);
        responseCompletes.of(requestResponseContext, null, missingContent, null, false).with(Response.of(Response.Status.BadRequest, e.getMessage()));
      } finally {
        buffer.release();
      }
    }

    private Request enrichRequest(final RequestResponseContext<?> requestResponseContext, final Request request) {
      try {
        request.headers.add(RequestHeader.of(XForwardedFor, requestResponseContext.remoteAddress()));
      } catch (final UnsupportedOperationException exception) {
        logger().error("Unable to enrich request headers");
      }
      return request;
    }


    @Override
    public void consume(final RequestResponseContext<?> requestResponseContext, final Request request) {
      consume(requestResponseContext, request, false);
    }

    private Context consume(
            final RequestResponseContext<?> requestResponseContext,
            final Request request,
            final boolean wasIncompleteContent) {

//    logger().debug("==============(" + instanceId + ") FULL REQUEST (" + (++fullCount) + "): \n" + unfilteredRequest);

      final boolean keepAlive = determineKeepAlive(requestResponseContext, request);
      final Request filteredRequest = filters.process(request);
      final Completes<Response> completes = responseCompletes.of(requestResponseContext, filteredRequest, false, filteredRequest.headers.headerOf(RequestHeader.XCorrelationID), keepAlive);
      final Context context = new Context(requestResponseContext, filteredRequest, world.completesFor(Returns.value(completes)));
      dispatcher.dispatchFor(context);

      if (wasIncompleteContent) {
        requestsMissingContent.remove(requestResponseContext.id());
      }

      return context;
    }

    private boolean determineKeepAlive(final RequestResponseContext<?> requestResponseContext, final Request unfilteredRequest) {
      final boolean keepAlive =
              unfilteredRequest
                .headerValueOr(RequestHeader.Connection, Header.ValueKeepAlive)
                .equalsIgnoreCase(Header.ValueKeepAlive);

//    if (keepAlive) {
//      logger().debug("///////// SERVER REQUEST KEEP ALIVE /////////(" + instanceId + ")");
//    } else {
//      logger().debug("///////// SERVER REQUEST EAGER CLOSE /////////(" + instanceId + ")");
//    }

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
    public Completes<Response> of(final RequestResponseContext<?> requestResponseContext, final Request request, final boolean missingContent, final Header correlationId, final boolean keepAlive) {
      return new BasicCompletedBasedResponseCompletes(requestResponseContext, request, missingContent, correlationId, keepAlive);
    }
  }

  private class BasicCompletedBasedResponseCompletes extends FutureCompletes<Response> {
    final Header correlationId;
    final boolean keepAlive;
    final boolean missingContent;
    final Request request;
    final RequestResponseContext<?> requestResponseContext;

    BasicCompletedBasedResponseCompletes(final RequestResponseContext<?> requestResponseContext, final Request request, final boolean missingContent, final Header correlationId, final boolean keepAlive) {
      super(stage().scheduler());
      this.requestResponseContext = requestResponseContext;
      this.request = request;
      this.missingContent = missingContent;
      this.correlationId = correlationId;
      this.keepAlive = keepAlive;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <O> Completes<O> with(final O response) {
      Response debugResponse = null;
      try {
        final Response unfilteredResponse = (Response) response;
        final Response filtered = filters.process(request, unfilteredResponse);
        final Response completedResponse = filtered.include(correlationId);
        debugResponse = completedResponse;
        final boolean closeAfterResponse = closeAfterResponse(unfilteredResponse);
        if (agent == null) {
          final ConsumerByteBuffer buffer = bufferFor(completedResponse);
          requestResponseContext.respondWith(completedResponse.into(buffer), closeAfterResponse);
        } else {
  //      System.out.println("============> SERVER RESPONSE: \n" + completedResponse);
          requestResponseContext.respondWith(completedResponse, closeAfterResponse);
        }
      } catch (Exception e) {
        final String message =
                "Failure responding to request because: " + e.getMessage() +
                "\nREQUEST:\n" + request +
                "\nRESPONSE:\n" + debugResponse;

        logger().error(message, e);
      }
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
//      logger().debug(
//              "///////// SERVER RESPONSE CLOSED FOLLOWING ///////// KEEP-ALIVE: " + keepAlive +
//              "\n///////// REQUEST\n" + request +
//              "\n///////// RESPONSE\n" + response);
        return keepAlive;
      }

      final boolean keepAliveAfterResponse =
              keepAlive ||
              response
                .headerValueOr(RequestHeader.Connection, Header.ValueKeepAlive)
                .equalsIgnoreCase(Header.ValueKeepAlive);

//   if (keepAliveAfterResponse) {
//      logger().debug("///////// SERVER RESPONSE KEEP ALIVE ////////");
//    } else {
//      logger().debug("///////// SERVER RESPONSE CLOSED FOLLOWING /////////\n" + response);
//    }

      return !keepAliveAfterResponse;
    }
  }
}
