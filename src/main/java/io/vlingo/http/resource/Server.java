// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import io.vlingo.actors.Address;
import io.vlingo.actors.Definition;
import io.vlingo.actors.Stage;
import io.vlingo.actors.Stoppable;

public interface Server extends Stoppable {

  public static Server startWith(final Stage stage) {
    final java.util.Properties properties = Properties.loadProperties();

    return startWith(stage, properties);
  }

  public static Server startWith(final Stage stage, java.util.Properties properties) {
    final int port = Integer.parseInt(properties.getProperty("server.http.port", "8080"));
    final int dispatcherPoolSize = Integer.parseInt(properties.getProperty("server.dispatcher.pool", "10"));
    final int maxBufferPoolSize = Integer.parseInt(properties.getProperty("server.buffer.pool.size", "100"));
    final int maxMessageSize = Integer.parseInt(properties.getProperty("server.message.buffer.size", "65535"));
    final int probeInterval = Integer.parseInt(properties.getProperty("server.probe.interval", "10"));
    final long probeTimeout = Long.parseLong(properties.getProperty("server.probe.timeout", "10"));
    final long requestMissingContentTimeout = Long.parseLong(properties.getProperty("server.request.missing.content.timeout", "100"));
    
    final Resources resources = Loader.loadResources(properties);
    
    return startWith(
            stage,
            resources,
            port,
            new Sizing(dispatcherPoolSize, maxBufferPoolSize, maxMessageSize),
            new Timing(probeInterval, probeTimeout, requestMissingContentTimeout));
  }

  public static Server startWith(
          final Stage stage,
          final Resources resources,
          final int port,
          final Sizing sizing,
          final Timing timing) {
    
    final Server server = stage.actorFor(
            Definition.has(
                    ServerActor.class,
                    Definition.parameters(resources, port, sizing, timing),
                    "queueMailbox",
                    ServerActor.ServerName),
            Server.class,
            Address.withHighId(),
            stage.world().defaultLogger());

    return server;
  }

  public static class Sizing {
    public final int dispatcherPoolSize;
    public final int maxBufferPoolSize;
    public final int maxMessageSize;
    
    public Sizing(final int dispatcherPoolSize, final int maxBufferPoolSize, final int maxMessageSize) {
      this.dispatcherPoolSize = dispatcherPoolSize;
      this.maxBufferPoolSize = maxBufferPoolSize;
      this.maxMessageSize = maxMessageSize;
    }
  }

  public static class Timing {
    public final long probeInterval;
    public final long probeTimeout;
    public final long requestMissingContentTimeout;
    
    public Timing(final int probeInterval, final long probeTimeout, final long requestMissingContentTimeout) {
      this.probeInterval = probeInterval;
      this.probeTimeout = probeTimeout;
      this.requestMissingContentTimeout = requestMissingContentTimeout;
    }
  }
}
