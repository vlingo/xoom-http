// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Cancellable;
import io.vlingo.actors.Completes;
import io.vlingo.actors.Scheduled;
import io.vlingo.actors.World;
import io.vlingo.http.Context;
import io.vlingo.http.RequestParser;
import io.vlingo.http.Response;
import io.vlingo.wire.channel.RequestChannelConsumer;
import io.vlingo.wire.channel.RequestResponseContext;
import io.vlingo.wire.fdx.bidirectional.ServerRequestResponseChannel;

public class ServerActor extends Actor implements Server, RequestChannelConsumer {
  private static final String ServerName = "vlingo-http-server";
  
  private final Cancellable cancellable;
  private final ServerRequestResponseChannel channel;
  private final Dispatcher[] dispatcherPool;
  private int dispatcherPoolIndex;
  private final World world;

  public ServerActor(
          final Resources resources,
          final int port,
          final int dispatcherPoolSize,
          final int maxBufferPoolSize,
          final int maxMessageSize,
          final int probeInterval,
          final long probeTimeout)
  throws Exception {
    this.dispatcherPoolIndex = 0;
    this.world = stage().world();

    try {
      this.dispatcherPool = new Dispatcher[dispatcherPoolSize];

      for (int idx = 0; idx < dispatcherPoolSize; ++idx) { 
        dispatcherPool[idx] = Dispatcher.startWith(stage(), resources);
      }

      this.channel = new ServerRequestResponseChannel(port, ServerName, maxBufferPoolSize, maxMessageSize, probeTimeout, logger());

      channel.openFor(this);

      logger().log("Server " + ServerName + " is listening on port: " + port);

      cancellable = stage().scheduler().schedule(selfAs(Scheduled.class), null, 0, probeInterval);

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
    pooledDispatcher()
      .dispatchFor(new Context(RequestParser.parse(requestResponseContext.requestBuffer().asByteBuffer()),
                   world.completesFor(new ResponseCompletes(requestResponseContext))));
  }


  //=========================================
  // Scheduled
  //=========================================

  @Override
  public void intervalSignal(final Scheduled scheduled, final Object data) {
    channel.probeChannel();
  }


  //=========================================
  // Stoppable
  //=========================================

  @Override
  public void stop() {
    cancellable.cancel();
    channel.close();
    
    for (final Dispatcher dispatcher : dispatcherPool) {
      dispatcher.stop();
    }
  }


  //=========================================
  // Stoppable
  //=========================================

  protected Dispatcher pooledDispatcher() {
    if (dispatcherPoolIndex >= dispatcherPool.length) {
      dispatcherPoolIndex = 0;
    }
    return dispatcherPool[dispatcherPoolIndex++];
  }


  //=========================================
  // internal implementation
  //=========================================

  private class ResponseCompletes implements Completes<Response> {
    final RequestResponseContext<?> requestResponseContext;

    ResponseCompletes(final RequestResponseContext<?> requestResponseContext) {
      this.requestResponseContext = requestResponseContext;
    }

    @Override
    public void with(final Response outcome) {
      requestResponseContext.respondOnceWith(outcome.into(requestResponseContext.requestBuffer()));
    }
  }
}
