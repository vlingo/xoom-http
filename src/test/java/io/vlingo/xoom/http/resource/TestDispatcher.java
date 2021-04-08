// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import io.vlingo.xoom.actors.Logger;
import io.vlingo.xoom.http.Context;

public class TestDispatcher implements Dispatcher {
  private final Logger logger;
  private final Resources resources;

  public TestDispatcher(final Resources resources, final Logger logger) {
    this.resources = resources;
    this.logger = logger;
  }

  @Override
  public void conclude() {
  }

  @Override
  public boolean isStopped() {
    return false;
  }

  @Override
  public void stop() {
  }

  @Override
  public void dispatchFor(final Context context) {
    resources.dispatchMatching(context, logger);
  }
}
