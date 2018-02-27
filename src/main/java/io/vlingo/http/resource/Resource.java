// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import io.vlingo.actors.Definition;
import io.vlingo.actors.Stage;
import io.vlingo.http.Context;
import io.vlingo.http.Method;
import io.vlingo.http.resource.Action.MappedParameters;
import io.vlingo.http.resource.Action.MatchResults;

public abstract class Resource<T> {
  final List<Action> actions;
  private final ResourceRequestHandler[] handlerPool;
  private final AtomicLong handlerPoolIndex;
  public final int handlerPoolSize;
  public final String name;
  public final Class<? extends ResourceHandler> resourceHandlerClass;

  public abstract void dispatchToHandlerWith(final Context context, final MappedParameters mappedParameters);

  void allocateHandlerPool(final Stage stage) {
    for (int idx = 0; idx < handlerPoolSize; ++idx) {
      handlerPool[idx] =
              stage.actorFor(
                      Definition.has(
                              ResourceRequestHandlerActor.class,
                              Definition.parameters(resourceHandlerInstance())),
                      ResourceRequestHandler.class);
    }
  }

  MatchResults matchWith(final Method method, final URI uri) {
    for (final Action action : actions) {
      final MatchResults matchResults = action.matchWith(method, uri);
      if (matchResults.isMatched()) {
        return matchResults;
      }
    }
    return Action.unmatchedResults;
  }

  protected Resource(
          final String name,
          final String resourceHandlerClassname,
          final int handlerPoolSize,
          final List<Action> actions) {
    
    this.name = name;
    this.resourceHandlerClass = loadResourceHandlerClass(resourceHandlerClassname);
    this.handlerPoolSize = handlerPoolSize;
    this.actions = Collections.unmodifiableList(actions);
    this.handlerPool = new ResourceRequestHandler[handlerPoolSize];
    this.handlerPoolIndex = new AtomicLong(0);
  }

  protected ResourceRequestHandler pooledHandler() {
    final int index = (int)(handlerPoolIndex.incrementAndGet() % handlerPoolSize);
    return handlerPool[index];
  }

  private void confirmResourceHandler(Class<?> resourceHandlerClass) {
    Class<?> superclass = resourceHandlerClass.getSuperclass();
    while (superclass != null) {
      if (superclass == ResourceHandler.class) {
        return;
      }
      superclass = superclass.getSuperclass();
    }
    throw new IllegalStateException("Resource handler class must extends ResourceHandler: " + resourceHandlerClass.getName());
  }

  @SuppressWarnings("unchecked")
  private Class<? extends ResourceHandler> loadResourceHandlerClass(final String resourceHandlerClassname) {
    try {
      final Class<? extends ResourceHandler> resourceHandlerClass = (Class<? extends ResourceHandler>) Class.forName(resourceHandlerClassname);
      confirmResourceHandler(resourceHandlerClass);
      return resourceHandlerClass;
    } catch (Exception e) {
      throw new IllegalArgumentException("The class for '" + resourceHandlerClassname + "' cannot be loaded.");
    }
  }

  private ResourceHandler resourceHandlerInstance() {
    try {
      return resourceHandlerClass.newInstance();
    } catch (Exception e) {
      throw new IllegalArgumentException("The instance for resource handler '" + resourceHandlerClass.getName() + "' cannot be created.");
    }
  }
}
