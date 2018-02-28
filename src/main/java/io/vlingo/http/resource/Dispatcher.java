// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import io.vlingo.actors.Stage;
import io.vlingo.http.Context;
import io.vlingo.http.Request;
import io.vlingo.http.resource.Action.MappedParameters;
import io.vlingo.http.resource.Action.MatchResults;

public class Dispatcher {
  private final Resources resources;
  private final Stage stage;

  static Dispatcher startWith(final Stage stage, final Resources resources) {
    return new Dispatcher(stage, resources);
  }

  void dispatchFor(final Context context) {
    final Request request = context.request;
    for (final Resource<?> resource : resources.namedResources.values()) {
      final MatchResults matchResults = resource.matchWith(request.method, request.uri);
      if (matchResults.isMatched()) {
        dispatchFor(context, resource, matchResults);
        return;
      }
    }
    throw new IllegalArgumentException("No matching resource for method " + context.request.method + " and URI " + context.request.uri);
  }

  void stop() {
  }

  private Dispatcher(final Stage stage, final Resources resources) {
    this.stage = stage;
    this.resources = resources;
    
    allocateHandlerPools();
  }

  private void allocateHandlerPools() {
    for (final Resource<?> resource : resources.namedResources.values()) {
      resource.allocateHandlerPool(stage);
    }
  }

  private void dispatchFor(final Context context, final Resource<?> resource, final MatchResults matchResults) {
    final MappedParameters mappedParameters = matchResults.action.map(context.request, matchResults.parameters());
    resource.dispatchToHandlerWith(context, mappedParameters);
  }
}
