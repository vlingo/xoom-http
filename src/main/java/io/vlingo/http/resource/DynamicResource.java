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
import java.util.stream.Collectors;

public class DynamicResource extends Resource<ResourceHandler> {
  final List<RequestHandler> handlers;
  private final List<Action> actions = new ArrayList<>();

  protected DynamicResource(final String name, final int handlerPoolSize, final List<RequestHandler> unsortedHandlers) {
    super(name, handlerPoolSize);
    this.handlers = sortHandlersBySlashes(unsortedHandlers);
    int currentId = 0;
    for(RequestHandler predicate: this.handlers) {
      actions.add(new Action(currentId++,
        predicate.method.toString(),
        predicate.path,
        "dynamic" + currentId + "(" + predicate.actionSignature + ")",
        null,
        false));
    }
  }

  public void dispatchToHandlerWith(final Context context, final Action.MappedParameters mappedParameters) {
    try {
      Consumer<ResourceHandler> consumer = (resource) ->
        handlers.get(mappedParameters.actionId)
                .execute(context.request, mappedParameters, resource.logger())
        .andThenConsume(context.completes::with);
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
