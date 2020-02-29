/*
 * Copyright © 2012-2020 VLINGO LABS. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.resource;

import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

import io.vlingo.actors.Definition;
import io.vlingo.actors.Logger;
import io.vlingo.actors.Stage;
import io.vlingo.http.Context;
import io.vlingo.http.Method;

/**
 * Specification of web resources that REST services can access - and what method will handle request.
 * Http body content can also be mapped. See {@link Action}
 * <p>
 * The resource location is composed by: URI + path parameters + query parameters
 * <p>
 * Contains an {@link Action} list containing [ {@link Method} , {@link URI}, java method ] that can be called from a client
 * <p>
 *
 * In example:
 * <ul>
 *   <li>GET, /organizations, ... ,queryOrganization </li>
 *   <li>POST, /organization, ... ,createOrganization </li>
 * </ul>
 *
 * @param <T> {@link ResourceHandler} type of class that contains the java methods to call when an {@link Action} is selected
 *           by the dispatcher and is going to be executed
 *           It is optional to specify the generic type when {@link Action} is used active
 */
public abstract class Resource<T> {
  public final String name;
  /**
   * Number of {@link ResourceRequestHandler} that can work with requests to the declared web URL concurrent.
   * When a Web Requests - a message -  comes (from outside the {@link io.vlingo.actors.World}) it will
   * easy take longer time for the underlying {@link ResourceHandler} to complete.
   * Therefore the {@link #handlerPool} provides true concurrent request execution via a pool
   * of {@link ResourceRequestHandler} that can work concurrent.
   */
  public final int handlerPoolSize;

  private final ResourceRequestHandler[] handlerPool;
  private final AtomicLong handlerPoolIndex;

  public abstract void dispatchToHandlerWith(final Context context, final Action.MappedParameters mappedParameters);

  abstract Action.MatchResults matchWith(final Method method, final URI uri);

  protected abstract void log(final Logger logger);

  protected abstract ResourceHandler resourceHandlerInstance(final Stage stage);

  void allocateHandlerPool(final Stage stage) {
    for (int idx = 0; idx < handlerPoolSize; ++idx) {
      handlerPool[idx] =
        stage.actorFor(
          ResourceRequestHandler.class,
          Definition.has(
            ResourceRequestHandlerActor.class,
            Definition.parameters(resourceHandlerInstance(stage))));
    }
  }

  protected ResourceRequestHandler pooledHandler() {
    final int index = (int) (handlerPoolIndex.incrementAndGet() % handlerPoolSize);
    return handlerPool[index];
  }

  Resource(final String name,
           final int handlerPoolSize) {
    this.name = name;
    this.handlerPoolSize = handlerPoolSize;
    this.handlerPool = new ResourceRequestHandler[handlerPoolSize];
    this.handlerPoolIndex = new AtomicLong(0);
  }

}
