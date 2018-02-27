// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import io.vlingo.http.Context;

public class Server {
  static Server instance;

  private final Dispatcher dispatcher;

  public static synchronized Server startWith(final Dispatcher dispatcher) {
    if (instance == null) {
      instance = new Server(dispatcher);
    }
    return instance;
  }

  public void dispatchFor(final Context context) {
    dispatcher.dispatchFor(context);
  }

  private Server(final Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }
}
