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
 * Each request to the web server will have own instance of this.
 * This means that many different ResourceHandler can be in progress concurrently.
 * <p>
 * A {@link ResourceHandler} can be seen as a "light" {@link io.vlingo.actors.Actor} -
 * that works in the specific http-server request handling -
 * it got access to {@link Scheduler} and similar services.
 * <p>
 * The system administrate resourceHandlers in pools. Therefore the same instance can be
 * meet again and again with different request.
 * <p>
 * Possible we should expose {@link #__internal__test_set_up} and explain this break of immutability
 */
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

  protected ContentType contentType() {
    return ContentType.of("text/plain", "us-ascii");
  }

  public void __internal__test_set_up(final Context context, final Stage stage) {
    this.context = context;
    this.stage = stage;
  }
}
