// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import io.vlingo.actors.Completes;
import io.vlingo.http.Context;
import io.vlingo.http.Response;

public abstract class ResourceHandler {
  Context context;

  protected ResourceHandler() {
  }

  protected Completes<Response> completes() {
    return context.completes;
  }

  protected Context context() {
    return context;
  }
}
