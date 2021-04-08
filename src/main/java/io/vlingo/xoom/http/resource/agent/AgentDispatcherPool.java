// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource.agent;

import java.util.concurrent.atomic.AtomicLong;

import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.http.resource.Dispatcher;
import io.vlingo.xoom.http.resource.DispatcherPool.AbstractDispatcherPool;
import io.vlingo.xoom.http.resource.Resources;

public class AgentDispatcherPool extends AbstractDispatcherPool {

  private AtomicLong dispatcherPoolIndex;
  private long dispatcherPoolSize;

  public AgentDispatcherPool(final Stage stage, final Resources resources, final int dispatcherPoolSize) {
    super(stage, resources, dispatcherPoolSize);

    this.dispatcherPoolIndex = new AtomicLong(-1);
    this.dispatcherPoolSize = dispatcherPoolSize;
  }

  @Override
  public Dispatcher dispatcher() {
    final int index = (int) (dispatcherPoolIndex.incrementAndGet() % dispatcherPoolSize);

    return dispatcherPool[index];
  }
}
