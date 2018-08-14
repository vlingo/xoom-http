/*
 * Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.resource;

import io.vlingo.actors.Stage;
import io.vlingo.http.Context;
import io.vlingo.http.Method;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DynamicResource extends Resource<ResourceHandler> {
  final List<Predicate> handlers;
  private final List<Action> actions = new ArrayList<>();

  protected DynamicResource(final String name, final int handlerPoolSize, final List<Predicate> handlers) {
    super(name, handlerPoolSize);
    this.handlers = handlers;
    int currentId = 0;
    for(Predicate predicate: handlers) {
      actions.add(new Action(currentId++,
        predicate.method.toString(),
        predicate.uri,
        "unused()",
        null,
        false));
    }
  }

  public void dispatchToHandlerWith(final Context context, final Action.MappedParameters mappedParameters) {
    try {
      Consumer<ResourceHandler> consumer = (resource) -> {
        resource.completes().with(
          handlers.get(mappedParameters.actionId).routeHandler.handler(context.request)
        );
      };
      pooledHandler().handleFor(context, consumer);
    } catch (Exception e) {
      throw new IllegalArgumentException("Action mismatch: Request: " + context.request + "Parameters: " + mappedParameters);
    }
  }

  Action.MatchResults matchWith(final Method method, final URI uri) {
    for (final Action action : actions) {
      final Action.MatchResults matchResults = action.matchWith(method, uri);
      if (matchResults.isMatched()) {
        return matchResults;
      }
    }
    return Action.unmatchedResults;
  }

  protected ResourceHandler resourceHandlerInstance(final Stage stage) {
    return new SpecificResourceHandler(stage);
  }

  private static class SpecificResourceHandler extends ResourceHandler {
    SpecificResourceHandler(final Stage stage) {
      this.stage = stage;
    }
  }
}
