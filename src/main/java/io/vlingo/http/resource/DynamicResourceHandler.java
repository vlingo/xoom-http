// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import io.vlingo.actors.Logger;
import io.vlingo.actors.Stage;

public abstract class DynamicResourceHandler {
  private final Logger logger;
  private final Stage stage;

  protected DynamicResourceHandler(final Stage stage) {
    this.stage = stage;
    this.logger = stage.world().defaultLogger();
  }

  public abstract Resource<?> routes();

  protected Logger logger() {
    return logger;
  }

  protected Stage stage() {
    return stage;
  }

}
