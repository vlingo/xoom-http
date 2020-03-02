package io.vlingo.http.resource;

import io.vlingo.actors.Actor;
import io.vlingo.http.Context;

/**
 * An {@code Actor} implementation of the {@code Dispatcher} for
 * non-blocking, asynchronous request dispatching.
 */
public class DispatcherActor extends Actor implements Dispatcher {
  private final Resources resources;

  /**
   * Constructs my state.
   * @param resources the Resources I manage and to which I dispatch when matched
   */
  public DispatcherActor(final Resources resources) {
    this.resources = resources;

    allocateHandlerPools();
  }

  /**
   * @see io.vlingo.http.resource.Dispatcher#dispatchFor(io.vlingo.http.Context)
   */
  @Override
  public void dispatchFor(final Context context) {
    resources.dispatchMatching(context, logger());
  }

  /**
   * @see io.vlingo.actors.Actor#stop()
   */
  @Override
  public void stop() {
    super.stop();
  }

  /**
   * Allocate a pool for each of my managed {@code Resource} instances,
   * each {@code Resource} having a configured pool size.
   */
  private void allocateHandlerPools() {
    for (final Resource<?> resource : resources.namedResources.values()) {
      resource.allocateHandlerPool(stage());
    }
  }
}
