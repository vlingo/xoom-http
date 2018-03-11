// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import io.vlingo.actors.Definition;
import io.vlingo.actors.Scheduled;
import io.vlingo.actors.Stage;
import io.vlingo.actors.Startable;
import io.vlingo.actors.Stoppable;

public interface Server extends Scheduled, Startable, Stoppable {

  public static Server startWith(final Stage stage) {
    final java.util.Properties properties = Properties.loadProperties();

    final int port = Integer.parseInt(properties.getProperty("server.http.port", "8080"));
    final int dispatcherPoolSize = Integer.parseInt(properties.getProperty("server.dispatcher.pool", "10"));
    final int maxBufferPoolSize = Integer.parseInt(properties.getProperty("server.buffer.pool.size", "100"));
    final int maxMessageSize = Integer.parseInt(properties.getProperty("server.message.buffer.size", "65535"));
    final int probeInterval = Integer.parseInt(properties.getProperty("server.probe.interval", "10"));
    final long probeTimeout = Long.parseLong(properties.getProperty("server.probe.timeout", "10"));
    
    final Resources resources = Loader.loadResources(properties);
    
    return startWith(stage, resources, port, dispatcherPoolSize, maxBufferPoolSize, maxMessageSize, probeInterval, probeTimeout);
  }

  public static Server startWith(
          final Stage stage,
          final Resources resources,
          final int port,
          final int dispatcherPoolSize,
          final int maxBufferPoolSize,
          final int maxMessageSize,
          final int probeInterval,
          final long probeTimeout) {
    
    final Server server = stage.actorFor(
            Definition.has(
                    ServerActor.class,
                    Definition.parameters(resources, port, dispatcherPoolSize, maxBufferPoolSize, maxMessageSize, probeInterval, probeTimeout)),
            Server.class);

    return server;
  }
}
