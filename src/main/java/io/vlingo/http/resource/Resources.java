// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.util.Collections;
import java.util.Map;

import io.vlingo.actors.Logger;
import io.vlingo.http.Context;
import io.vlingo.http.resource.Action.MappedParameters;
import io.vlingo.http.resource.Action.MatchResults;

public class Resources {
  final Map<String,Resource<?>> namedResources;
  
  @Override
  public String toString() {
    return "Resources[namedResources=" + namedResources + "]";
  }
  
  Resources(final Map<String,Resource<?>> namedResources) {
    this.namedResources = Collections.unmodifiableMap(namedResources);
  }

  Resource<?> resourceOf(final String name) {
    return namedResources.get(name);
  }

  void dispatchMatching(final Context context, Logger logger) {
    for (final Resource<?> resource : namedResources.values()) {
      final MatchResults matchResults = resource.matchWith(context.request.method, context.request.uri);
      if (matchResults.isMatched()) {
        final MappedParameters mappedParameters = matchResults.action.map(context.request, matchResults.parameters());
        resource.dispatchToHandlerWith(context, mappedParameters);
        return;
      }
    }
    logger.log("No matching resource for method " + context.request.method + " and URI " + context.request.uri);
  }
}
