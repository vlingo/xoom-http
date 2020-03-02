// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import io.vlingo.actors.CompletesEventually;
import io.vlingo.wire.channel.RequestResponseContext;

/**
 * The context of a request-response within the server. This provides the
 * {@code Request} instance with headers and body, the means to make a
 * {@code Response} through the communications channel, and to make the
 * handling of the request and the production of the response asynchronous
 * by completing eventually.
 */
public class Context {
  /** My {@code CompletesEventually} instance. */
  public final CompletesEventually completes;
  /** My {@code Request} instance. */
  public final Request request;
  /** My {@code RequestResponseContext<?>} instance. */
  private final RequestResponseContext<?> requestResponseContext;

  /**
   * Construct my state.
   * @param requestResponseContext the {@code RequestResponseContext<?>} providing channel communication
   * @param request the Request from the client
   * @param completes the CompletesEventually through which the Response is eventually provided
   */
  public Context(final RequestResponseContext<?> requestResponseContext, final Request request, final CompletesEventually completes) {
    this.requestResponseContext = requestResponseContext;
    this.request = request;
    this.completes = completes;
  }

  /**
   * Construct my state.
   * @param request the Request from the client
   * @param completes the CompletesEventually through which the Response is eventually provided
   */
  public Context(final Request request, final CompletesEventually completes) {
    this(null, request, completes);
  }

  /**
   * Construct my state.
   * @param completes the CompletesEventually through which the Response is eventually provided
   */
  public Context(final CompletesEventually completes) {
    this(null, completes);
  }

  /**
   * Answer my {@code requestResponseContext} as the {@code clientContext}.
   * @return {@code RequestResponseContext<?>}
   */
  public RequestResponseContext<?> clientContext() {
    return requestResponseContext;
  }

  /**
   * Answer whether or not I was instantiated with a {@code requestResponseContext}.
   * @return boolean
   */
  public boolean hasClientContext() {
    return requestResponseContext != null;
  }

  /**
   * Answer whether or not I was instantiated with a {@code request}.
   * @return boolean
   */
  public boolean hasRequest() {
    return request != null;
  }

  /**
   * Answer my {@code request}, which may be {@code null}.
   * @return Request
   */
  public Request request() {
    return request;
  }
}
