// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import io.vlingo.actors.Definition;
import io.vlingo.actors.Stage;
import io.vlingo.actors.Stoppable;
import io.vlingo.common.Completes;
import io.vlingo.http.resource.Configuration.Sizing;
import io.vlingo.http.resource.Configuration.Timing;

public interface Server extends Stoppable {

  public static Server startWith(final Stage stage) {
    return startWith(stage, Properties.properties);
  }

  public static Server startWith(final Stage stage, java.util.Properties properties) {
    final Configuration configuration = Configuration.defineWith(properties);
    
    final Resources resources = Loader.loadResources(properties);
    
    return startWith(
            stage,
            resources,
            configuration.port(),
            configuration.sizing(),
            configuration.timing());
  }

  public static Server startWith(
          final Stage stage,
          final Resources resources,
          final int port,
          final Sizing sizing,
          final Timing timing) {
    
    final Server server = stage.actorFor(
            Server.class,
            Definition.has(
                    ServerActor.class,
                    Definition.parameters(resources, port, sizing, timing),
                    "queueMailbox",
                    ServerActor.ServerName),
            stage.world().addressFactory().withHighId(),
            stage.world().defaultLogger());

    server.startUp();

    return server;
  }

  Completes<Boolean> shutDown();
  Completes<Boolean> startUp();
}
