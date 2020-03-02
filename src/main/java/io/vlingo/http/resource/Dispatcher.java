// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import io.vlingo.actors.ActorInstantiator;
import io.vlingo.actors.Definition;
import io.vlingo.actors.Stage;
import io.vlingo.actors.Stoppable;
import io.vlingo.http.Context;

/**
 * The protocol provided by {@code Server} request dispatchers, such as {@code DispatcherActor}.
 */
public interface Dispatcher extends Stoppable {
  /**
   * Answer a new {@code Dispatcher} backed by {@code DispatcherActor} within {@code Stage}
   * and assigned to manage dispatching to the given {@code Resources}.
   * @param stage the Stage within which the new DispatcherActor will reside
   * @param resources the Resources that may be dispatched
   * @return Dispatcher
   */
  public static Dispatcher startWith(final Stage stage, final Resources resources) {
    final Dispatcher dispatcher =
            stage.actorFor(
                    Dispatcher.class,
                    Definition.has(DispatcherActor.class, new DispatcherInstantiator(resources)));

    return dispatcher;
  }

  /**
   * Dispatches the request provided by the given {@code context} to one of my {@code resources}.
   * @param context the Context holding the Request to be handled
   */
  void dispatchFor(final Context context);

  /**
   * The {@code ActorInstantiator} for {@code Dispatcher} instances.
   */
  static class DispatcherInstantiator implements ActorInstantiator<DispatcherActor> {
    private static final long serialVersionUID = 9025560076715268682L;

    private final Resources resources;

    public DispatcherInstantiator(final Resources resources) {
      this.resources = resources;
    }

    @Override
    public DispatcherActor instantiate() {
      return new DispatcherActor(resources);
    }

    @Override
    public Class<DispatcherActor> type() {
      return DispatcherActor.class;
    }
  }
}
