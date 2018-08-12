// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import io.vlingo.actors.CompletesEventually;
import io.vlingo.actors.Stage;
import io.vlingo.http.Context;

public abstract class ResourceHandler {
  Context context;
  Stage stage;

  protected ResourceHandler() {
  }

  protected CompletesEventually completes() {
    return context.completes;
  }

  protected Context context() {
    return context;
  }

  protected Stage stage() {
    return stage;
  }
}
