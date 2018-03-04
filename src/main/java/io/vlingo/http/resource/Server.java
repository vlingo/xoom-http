// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.util.concurrent.atomic.AtomicLong;

import io.vlingo.actors.Stage;
import io.vlingo.http.Context;

public class Server {
  private final Dispatcher[] dispatcherPool;
  private final AtomicLong dispatcherPoolIndex;
  private final int dispatcherPoolSize;

  public static Server startWith(final Stage stage) {
    final java.util.Properties properties = Properties.loadProperties();

    final int dispatcherPoolSize = Integer.parseInt(properties.getProperty("server.dispatcher.pool", "10"));
    
    final Resources resources = Loader.loadResources(properties);
    
    return startWith(resources, stage, dispatcherPoolSize);
  }

  public static Server startWith(final Resources resources, final Stage stage, final int dispatcherPoolSize) {
    return new Server(resources, stage, dispatcherPoolSize);
  }

  public void dispatchFor(final Context context) {
    pooledDispatcher().dispatchFor(context);
  }

  public void stop() {
    for (final Dispatcher dispatcher : dispatcherPool) {
      dispatcher.stop();
    }
  }

  private Server(final Resources resources, final Stage stage, final int dispatcherPoolSize) {
    this.dispatcherPoolSize = dispatcherPoolSize;
    this.dispatcherPoolIndex = new AtomicLong(0);
    
    this.dispatcherPool = new Dispatcher[dispatcherPoolSize];
    
    for (int idx = 0; idx < dispatcherPoolSize; ++idx) { 
      dispatcherPool[idx] = Dispatcher.startWith(stage, resources);
    }
  }

  protected Dispatcher pooledDispatcher() {
    final int index = (int)(dispatcherPoolIndex.incrementAndGet() % dispatcherPoolSize);
    return dispatcherPool[index];
  }
}
