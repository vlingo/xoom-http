// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import io.vlingo.xoom.actors.Stage;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A pool of {@code Dispatcher} instances.
 */
public interface DispatcherPool {
  /**
   * Close the {@code Dispatcher} instances of my internal pool.
   */
  void close();

  /**
   * Answer an available {@code Dispatcher} from my pool.
   * @return Dispatcher
   */
  Dispatcher dispatcher();

  /**
   * Default behavior for all {@code DispatcherPool} implementations.
   */
  static abstract class AbstractDispatcherPool implements DispatcherPool {
    protected final Dispatcher[] dispatcherPool;

    protected AtomicLong dispatcherPoolIndex;
    protected long dispatcherPoolSize;

    protected AbstractDispatcherPool(final Stage stage, final Resources resources, final int dispatcherPoolSize) {
      this.dispatcherPool = new Dispatcher[dispatcherPoolSize];

      for (int idx = 0; idx < dispatcherPoolSize; ++idx) {
        dispatcherPool[idx] = Dispatcher.startWith(stage, resources);
      }
    }

    @Override
    public void close() {
      for (final Dispatcher dispatcher : dispatcherPool) {
        dispatcher.stop();
      }
    }
  }
}
