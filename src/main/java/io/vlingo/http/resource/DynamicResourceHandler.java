// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
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
import io.vlingo.http.ContentType;
import io.vlingo.http.Context;

/**
 * An optional base class that may be used by resources configured using
 * the fluent API provided by {@code ResourceBuilder}.
 */
public abstract class DynamicResourceHandler {
  private Context context;
  private final Stage stage;

  /**
   * Constructs my default state with the {@code Stage}.
   * @param stage the Stage that manages my state and Actor-based execution
   */
  protected DynamicResourceHandler(final Stage stage) {
    this.stage = stage;
  }

  /**
   * Answer the {@code Resource<?>} that maps my behaviors, which are fluently
   * configured using a {@code ResourceBuilder}.
   * @return {@code Resource<?>}
   */
  public abstract Resource<?> routes();

  /**
   * Answer my {@code completes}.
   * @return CompletesEventually
   */
  protected CompletesEventually completes() {
    return context.completes;
  }

  /**
   * Answer my {@code contentType}, which is by default {@code "text/plain", "us-ascii"}.
   * @return ContentType
   */
  protected ContentType contentType() {
    return ContentType.of("text/plain", "us-ascii");
  }

  /**
   * Answer my {@code context}.
   * @return Context
   */
  protected Context context() {
    return context;
  }

  /**
   * Answer my {@code logger}, which is the {@code defaultLogger} of my {@code World}.
   * @return Logger
   */
  protected Logger logger() {
    return stage.world().defaultLogger();
  }

  /**
   * Answer my {@code scheduler}, which is owned by my {@code Stage}.
   * @return Scheduler
   */
  protected Scheduler scheduler() {
    return stage.scheduler();
  }

  /**
   * Answer my {@code stage}, within which my backing {@code Actor} resides.
   * @return Stage
   */
  protected Stage stage() {
    return stage;
  }

  /**
   * Used by the internal {@code Server} runtime to set my {@code Context}
   * for the current {@code Request} that I will be subsequently handling.
   * @param context the Context for the current Request that I will be subsequently handling
   */
  void context(final Context context) {
    this.context = context;
  }
}
