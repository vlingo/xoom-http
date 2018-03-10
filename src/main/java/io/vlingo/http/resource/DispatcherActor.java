package io.vlingo.http.resource;

import io.vlingo.actors.Actor;
import io.vlingo.http.Context;

public class DispatcherActor extends Actor implements Dispatcher {
  private final Resources resources;

  public DispatcherActor(final Resources resources) {
    this.resources = resources;
    
    allocateHandlerPools();
  }

  @Override
  public void dispatchFor(final Context context) {
    resources.dispatchMatching(context, logger());
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
