// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.http.Filters;
import io.vlingo.xoom.http.resource.Configuration.Sizing;
import io.vlingo.xoom.http.resource.Configuration.Timing;
import io.vlingo.xoom.http.sample.user.ProfileResourceFluent;
import io.vlingo.xoom.http.sample.user.UserResourceFluent;

public class ServerBootstrap {
  public static ServerBootstrap instance;

  public final World world;
  public final Server server;

  public static void main(final String[] args) {
    instance = new ServerBootstrap();
  }

  private ServerBootstrap() {
    world = World.start("xoom-http-server");

    final UserResourceFluent userResource = new UserResourceFluent(world);
    final ProfileResourceFluent profileResource = new ProfileResourceFluent(world);
    final Resource<?> r1 = userResource.routes();
    final Resource<?> r2 = profileResource.routes();
    final Resources resources = Resources.are(r1, r2);

    server =
            Server.startWith(
                    world.stage(),
                    resources,
                    Filters.none(),
                    8081,
                    Sizing.defineWith(4, 10, 100, 10240),
                    Timing.defineWith(3, 1, 100),
                    "arrayQueueMailbox",
                    "arrayQueueMailbox");

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        if (instance != null) {
          instance.server.stop();

          System.out.println("\n");
          System.out.println("==============================");
          System.out.println("Stopping vlingo/http Server...");
          System.out.println("==============================");
        }
      }
    });
  }
}
