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

public class RequestHandler1<T> {
  final Method method;
  final String path;
  private Handler1<T> handler;

  public RequestHandler1(final Method method, final String path) {
    this.method = method;
    this.path = path;
  }

  @FunctionalInterface
  interface Handler1<T> {
    Response execute(T param1);
  }

  public RequestHandler1<T> handle(final Handler1<T> handler) {
    this.handler = handler;
    return this;
  }

  public Response execute(final T param1) {
    if(this.handler == null) throw new HandlerMissingException("No handle defined for " + method.toString() + " " + path);
    return this.handler.execute(param1);
  }
}
