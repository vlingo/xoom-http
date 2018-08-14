// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.vlingo.actors.Logger;
import io.vlingo.http.Context;
import io.vlingo.http.Response;
import io.vlingo.http.resource.Action.MappedParameters;
import io.vlingo.http.resource.Action.MatchResults;

public class Resources {
  final Map<String, Resource<?>> namedResources;

  public static Resources are(final Resource<?>... resources) {
    final Resources all = new Resources();
    for (final Resource<?> resource : resources) {
      all.namedResources.put(resource.name, resource);
    }
    return all;
  }

//  public Resources ready() {
//    return new Resources(namedResources);
//  }

//  public Resources add(final Resource<?> resource) {
//    namedResources.put(resource.name, resource);
//    return this;
//  }

  @Override
  public String toString() {
    return "Resources[namedResources=" + namedResources + "]";
  }

  Resources(final Map<String, Resource<?>> namedResources) {
    this.namedResources = Collections.unmodifiableMap(namedResources);
  }

  Resources(final Resource<?> resource) {
    this.namedResources = new HashMap<>();
    this.namedResources.put(resource.name, resource);
  }

  private Resources() {
    this.namedResources = new HashMap<>();
  }

  Resource<?> resourceOf(final String name) {
    return namedResources.get(name);
  }

  void dispatchMatching(final Context context, Logger logger) {
    String message;

    try {
      for (final Resource<?> resource : namedResources.values()) {
        final MatchResults matchResults = resource.matchWith(context.request.method, context.request.uri);
        if (matchResults.isMatched()) {
          final MappedParameters mappedParameters = matchResults.action.map(context.request, matchResults.parameters());
          resource.dispatchToHandlerWith(context, mappedParameters);
          return;
        }
      }
      message = "No matching resource for method " + context.request.method + " and URI " + context.request.uri;
      logger.log(message);
    } catch (Exception e) {
      message = "Problem dispatching request for method " + context.request.method + " and URI " + context.request.uri + " because: " + e.getMessage();
      logger.log(message, e);
    }

    context.completes.with(Response.of(Response.Status.NotFound, message));
  }
}
