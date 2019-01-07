// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import io.vlingo.actors.Definition;
import io.vlingo.actors.Stage;
import io.vlingo.actors.Stoppable;
import io.vlingo.http.Context;

public interface Dispatcher extends Stoppable {
  public static Dispatcher startWith(final Stage stage, final Resources resources) {
    final Dispatcher dispatcher =
            stage.actorFor(
                    Dispatcher.class,
                    Definition.has(DispatcherActor.class, Definition.parameters(resources)));

    return dispatcher;
  }

  void dispatchFor(final Context context);
}
