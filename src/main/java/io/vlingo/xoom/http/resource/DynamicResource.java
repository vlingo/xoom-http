/*
 * Copyright © 2012-2023 VLINGO LABS. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.xoom.http.resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.vlingo.xoom.actors.Logger;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.http.Context;
import io.vlingo.xoom.http.Method;

public class DynamicResource extends Resource<ResourceHandler> {
  final DynamicResourceHandler dynamicResourceHandler;
  final List<RequestHandler> handlers;
  private final List<Action> actions = new ArrayList<>();

  protected DynamicResource(final String name, final DynamicResourceHandler dynamicResourceHandler, final int handlerPoolSize, final List<RequestHandler> unsortedHandlers) {
    super(name, handlerPoolSize);
    this.dynamicResourceHandler = dynamicResourceHandler;
    this.handlers = sortHandlersBySlashes(unsortedHandlers);
    int currentId = 0;
    for(RequestHandler predicate: this.handlers) {
      actions.add(new Action(currentId++,
        predicate.method.toString(),
        predicate.path,
        "dynamic" + currentId + "(" + predicate.actionSignature + ")",
        null));
    }
  }

  protected DynamicResource(final String name, final int handlerPoolSize, final List<RequestHandler> unsortedHandlers) {
    this(name, null, handlerPoolSize, unsortedHandlers);
  }

  @Override
  public void dispatchToHandlerWith(final Context context, final Action.MappedParameters mappedParameters) {
    try {
      final RequestHandler handler = handlers.get(mappedParameters.actionId);
      if (dynamicResourceHandler != null) dynamicResourceHandler.context(context);
      pooledHandler().handleFor(context, mappedParameters, handler);
    } catch (Exception e) {
      throw new IllegalArgumentException("Action mismatch: Request: " + context.request + "Parameters: " + mappedParameters);
    }
  }

  @Override
  Action.MatchResults matchWith(final Method method, final URI uri) {
    for (final Action action : actions) {
      final Action.MatchResults matchResults = action.matchWith(method, uri);
      if (matchResults.isMatched()) {
        return matchResults;
      }
    }
    return Action.unmatchedResults;
  }

  @Override
  protected void log(final Logger logger) {
    logger.info("Resource: " + name);

    for (final Action action : actions) {
      logger.info("Action: id=" + action.id + ", method=" + action.method + ", uri=" + action.uri + ", to=" + action.to.signature());
    }
  }

  @Override
  protected ResourceHandler resourceHandlerInstance(final Stage stage) {
    return new PooledDynamicResourceHandler(stage, this);
  }

  private static class PooledDynamicResourceHandler extends ResourceHandler {
    @SuppressWarnings("unused")
    private final DynamicResource resource;

    PooledDynamicResourceHandler(final Stage stage, final DynamicResource resource) {
      this.stage = stage;
      this.resource = resource;
    }
  }


  private List<RequestHandler> sortHandlersBySlashes(List<RequestHandler> unsortedHandlers) {
    return unsortedHandlers
      .stream()
      .sorted((handler1, handler2) -> {
        final Long handler1Slashes = handler1.path.chars().filter(ch -> ch == '/').count();
        final Long handler2Slashes = handler2.path.chars().filter(ch -> ch == '/').count();
        if (handler1Slashes.equals(handler2Slashes))
          return 0;
        return handler1Slashes < handler2Slashes ? 1 : -1;
      })
      .collect(Collectors.toList());
  }
}
