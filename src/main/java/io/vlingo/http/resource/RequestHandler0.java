/*
 * Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.resource;

import io.vlingo.http.Method;
import io.vlingo.http.Response;

public class RequestHandler0 {
  final Method method;
  final String path;
  private Handler0 handler;

  RequestHandler0(final Method method, final String path) {
    this.method = method;
    this.path = path;
  }

  public <T> RequestHandler1<T> param1(Class<T> paramClass) {
    return new RequestHandler1<>(this.method, this.path, paramClass);
  }

  @FunctionalInterface
  interface Handler0 {
    Response execute();
  }

  public RequestHandler0 handle(final Handler0 handler) {
    this.handler = handler;
    return this;
  }

  Response execute() {
    if(this.handler == null) throw new HandlerMissingException("No handle defined for " + method.toString() + " " + path);
    return this.handler.execute();
  }
}
