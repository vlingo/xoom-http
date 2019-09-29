// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.util.function.Consumer;

import io.vlingo.actors.Actor;
import io.vlingo.http.Context;
import io.vlingo.http.Response;
import io.vlingo.http.resource.Action.MappedParameters;

public class ResourceRequestHandlerActor extends Actor implements ResourceRequestHandler {
  private final ResourceHandler resourceHandler;

  public ResourceRequestHandlerActor(final ResourceHandler resourceHandler) {
    this.resourceHandler = resourceHandler;
  }

  @Override
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void handleFor(final Context context, final Consumer consumer) {
    try {
      resourceHandler.context = context;
      resourceHandler.stage = stage();
      consumer.accept(resourceHandler);
    }catch (Error throwable) {
      logger().error("Error thrown by resource dispatcher", throwable);
      context.completes.with(Response.of(Response.Status.InternalServerError));
    }catch (RuntimeException exception) {
      logger().error("Runtime thrown by resource dispatcher", exception);
      context.completes.with(Response.of(Response.Status.InternalServerError));
    }
  }

  @Override
  public void handleFor(final Context context, final MappedParameters mappedParameters, final RequestHandler handler) {
    context.__internal__resourceHandlerId = resourceHandler.resourceHandlerId();

    final Consumer<ResourceHandler> consumer = (resource) ->
      handler
        .execute(context.request, mappedParameters, resource.logger())
        .andFinallyConsume(context.completes::with);

    handleFor(context, consumer);
  }
}
