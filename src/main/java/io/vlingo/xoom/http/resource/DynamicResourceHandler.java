// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import io.vlingo.xoom.actors.CompletesEventually;
import io.vlingo.xoom.actors.Logger;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.common.Scheduler;
import io.vlingo.xoom.http.ContentType;
import io.vlingo.xoom.http.Context;
import io.vlingo.xoom.http.Header.Headers;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.Response.Status;
import io.vlingo.xoom.http.ResponseHeader;

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
   * Answer a {@code Response} with the {@code status} and {@code entity}
   * with a {@code Content-Type} header per my {@code contentType()}, which
   * may be overridden.
   * @param status the Status of the Response
   * @param entity the String entity of the Response
   * @return Response
   */
  protected Response entityResponseOf(final Status status, final String entity) {
    return entityResponseOf(status, Headers.empty(), entity);
  }

  /**
   * Answer a {@code Response} with the {@code status}, {@code headers}, and {@code entity}
   * with a {@code Content-Type} header per my {@code contentType()}, which may be overridden.
   * @param status the Status of the Response
   * @param headers the {@code Headers<ResponseHeader>} to which the {@code Content-Type} header is appended
   * @param entity the String entity of the Response
   * @return Response
   */
  protected Response entityResponseOf(final Status status, final Headers<ResponseHeader> headers, final String entity) {
    return Response.of(status, headers.and(contentType().toResponseHeader()), entity);
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
