package io.vlingo.http.resource;

import io.vlingo.actors.Actor;
import io.vlingo.http.Context;
import io.vlingo.http.Request;
import io.vlingo.http.resource.Action.MappedParameters;
import io.vlingo.http.resource.Action.MatchResults;

public class DispatcherActor extends Actor implements Dispatcher {
  private final Resources resources;

  public DispatcherActor(final Resources resources) {
    this.resources = resources;
    
    allocateHandlerPools();
  }

  @Override
  public void dispatchFor(final Context context) {
    final Request request = context.request;
    for (final Resource<?> resource : resources.namedResources.values()) {
      final MatchResults matchResults = resource.matchWith(request.method, request.uri);
      if (matchResults.isMatched()) {
        final MappedParameters mappedParameters = matchResults.action.map(context.request, matchResults.parameters());
        resource.dispatchToHandlerWith(context, mappedParameters);
        return;
      }
    }
    logger().log("No matching resource for method " + context.request.method + " and URI " + context.request.uri);
  }

  @Override
  public void stop() {
    super.stop();
  }

  private void allocateHandlerPools() {
    for (final Resource<?> resource : resources.namedResources.values()) {
      resource.allocateHandlerPool(stage());
    }
  }
}
