/*
 * Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.resource;

import io.vlingo.actors.Definition;
import io.vlingo.actors.Stage;
import io.vlingo.http.Context;
import io.vlingo.http.Method;

import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Resource<T> {
  public final String name;
  public final ThreadLocal<ResourceRequestHandler> handler;
  private Stage stageForPool;

  public abstract void dispatchToHandlerWith(final Context context, final Action.MappedParameters mappedParameters);

  abstract Action.MatchResults matchWith(final Method method, final URI uri);

  protected abstract ResourceHandler resourceHandlerInstance(final Stage stage);

  void allocateHandlerPool(final Stage stage) {
    stageForPool = stage;
  }

  protected ResourceRequestHandler pooledHandler() {
    ResourceRequestHandler resourceHandler = handler.get();
    if (resourceHandler == null) {
      resourceHandler = stageForPool.actorFor(
        Definition.has(
          ResourceRequestHandlerActor.class,
          Definition.parameters(resourceHandlerInstance(stageForPool))),
        ResourceRequestHandler.class);

      handler.set(resourceHandler);
    }

    return resourceHandler;
  }

  Resource(final String name, final int unused) {
    this.name = name;
    this.handler = ThreadLocal.withInitial(() -> null);
  }

}
