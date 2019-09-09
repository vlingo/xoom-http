// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import io.vlingo.actors.CompletesEventually;
import io.vlingo.actors.Logger;
import io.vlingo.actors.Stage;
import io.vlingo.common.Scheduler;
import io.vlingo.http.Context;

public abstract class ResourceHandler {
  Context context;
  Stage stage;

  /**
   * Answer the {@code Resource<?>} fluently defined by the {@code ResourceBuilder} DSL.
   * Must be overridden to use.
   * @return {@code Resource<?>}
   */
  public Resource<?> routes() {
    throw new UnsupportedOperationException("Undefined resource; must override.");
  }

  protected ResourceHandler() {
  }

  protected CompletesEventually completes() {
    return context.completes;
  }

  protected Context context() {
    return context;
  }

  protected Logger logger() {
    return stage.world().defaultLogger();
  }

  protected Scheduler scheduler() {
    return stage.scheduler();
  }

  protected Stage stage() {
    return stage;
  }

  public void __internal__test_set_up(final Context context, final Stage stage) {
    this.context = context;
    this.stage = stage;
  }
}
