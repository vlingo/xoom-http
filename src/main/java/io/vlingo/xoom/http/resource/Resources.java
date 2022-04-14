// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.vlingo.xoom.actors.Logger;
import io.vlingo.xoom.http.Context;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.resource.Action.MappedParameters;
import io.vlingo.xoom.http.resource.Action.MatchResults;

/**
 * Holds a number of named {@code Resource} instances and provides the means to match
 * the incoming {@code Request} (via URI) to an available {@code Resource}.
 */
public class Resources {
  final Map<String, Resource<?>> namedResources;

  /**
   * Answer a new {@code Resources} that holes the given individual {@code Resource} instances.
   * @param resources the {@code Resource<?>} varargs to assign to the new Resources
   * @return Resources
   */
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

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Resources[namedResources=" + namedResources + "]";
  }

  /**
   * Construct my state.
   * @param namedResources the {@code Map<String, Resource<?>>} of named Resource instances
   */
  Resources(final Map<String, Resource<?>> namedResources) {
    this.namedResources = Collections.unmodifiableMap(namedResources);
  }

  /**
   * Construct my state.
   * @param resource my single {@code Resource<?>} to manage
   */
  Resources(final Resource<?> resource) {
    this.namedResources = new HashMap<>();
    this.namedResources.put(resource.name, resource);
  }

  /**
   * Construct my state with no resource instance.
   */
  private Resources() {
    this.namedResources = new HashMap<>();
  }

  /**
   * Answer the {@code Resource<?>} that has the matching {@code name}, if any.
   * @param name the String name of the {@code Resource<?>} to find
   * @return {@code Resource<?>}
   */
  Resource<?> resourceOf(final String name) {
    return namedResources.get(name);
  }

  /**
   * Dispatch the {@code Request} held by the {@code Context} and matching
   * one of my managed resource instances, or log a warning if no match.
   * Returns immediately following the non-blocking dispatch.
   * @param context the Context containing the Request to match
   * @param logger the Logger to log potential warnings and errors
   */
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
      logger.warn(message);
    } catch (Exception e) {
      message = "Problem dispatching request for method " + context.request.method + " and URI " + context.request.uri + " because: " + e.getMessage();
      logger.error(message, e);
    }

    context.completes.with(Response.of(Response.Status.NotFound, message));
  }
}
