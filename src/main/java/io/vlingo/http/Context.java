// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import io.vlingo.actors.CompletesEventually;

public class Context {
  public final CompletesEventually completes;
  public final Request request;

  public Context(final Request request, final CompletesEventually completes) {
    this.request = request;
    this.completes = completes;
  }

  public Context(final CompletesEventually completes) {
    this(null, completes);
  }

  public boolean hasRequest() {
    return request != null;
  }

  public Request request() {
    return request;
  }
}
