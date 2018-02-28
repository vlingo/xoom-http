// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import io.vlingo.http.Context;

public class Server {
  private final Dispatcher dispatcher;

  public static Server startWith(final Dispatcher dispatcher) {
    return new Server(dispatcher);
  }

  public void dispatchFor(final Context context) {
    dispatcher.dispatchFor(context);
  }

  public void stop() {
    dispatcher.stop();
  }

  private Server(final Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }
}
