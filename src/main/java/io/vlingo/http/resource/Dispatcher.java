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
 * Dispatcher is an adapter ( technical actor) that is able to take a http request in form
 * of a message inside {@link Context} and make sure that the request is handled
 * <p>
 * There is a dispatcher for each {@link Resource} and the dispatcher will typically send
 * the message to a pooled {@link ResourceRequestHandler} that can handle the message.
 */
public interface Dispatcher extends Stoppable {
  static Dispatcher startWith(final Stage stage, final Resources resources) {
    final Dispatcher dispatcher =
            stage.actorFor(
                    Dispatcher.class,
                    Definition.has(DispatcherActor.class, new DispatcherInstantiator(resources)));

    return dispatcher;
  }

  void dispatchFor(final Context context);

  class DispatcherInstantiator implements ActorInstantiator<DispatcherActor> {
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
