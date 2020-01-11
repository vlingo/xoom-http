// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import io.vlingo.actors.CompletesEventually;
import io.vlingo.wire.channel.RequestResponseContext;

public class Context {
  public final CompletesEventually completes;
  public final Request request;
  private final RequestResponseContext<?> requestResponseContext;

  public Context(final RequestResponseContext<?> requestResponseContext, final Request request, final CompletesEventually completes) {
    this.requestResponseContext = requestResponseContext;
    this.request = request;
    this.completes = completes;
  }

  public Context(final Request request, final CompletesEventually completes) {
    this(null, request, completes);
  }

  public Context(final CompletesEventually completes) {
    this(null, completes);
  }

  public RequestResponseContext<?> clientContext() {
    return requestResponseContext;
  }

  public boolean hasClientContext() {
    return requestResponseContext != null;
  }

  public boolean hasRequest() {
    return request != null;
  }

  public Request request() {
    return request;
  }
}
