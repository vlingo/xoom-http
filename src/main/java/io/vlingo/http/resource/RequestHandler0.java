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
import io.vlingo.http.Request;
import io.vlingo.http.Response;

public class RequestHandler0 extends RequestHandler {
  private Handler0 handler;

  RequestHandler0(final Method method, final String path) {
    super(method, path);
  }

  public <T> RequestHandler1<T> param(Class<T> paramClass) {
    return new RequestHandler1<>(method(), path(), paramClass);
  }

  @FunctionalInterface
  public interface Handler0 {
    Response execute();
  }

  public RequestHandler0 handle(final Handler0 handler) {
    this.handler = handler;
    return this;
  }

  Response execute() {
    if(handler == null) throw new HandlerMissingException("No handle defined for " + method().toString() + " " + path());
    return handler.execute();
  }

  @Override
  Response execute(Request request, Action.MappedParameters mappedParameters) {
    return execute();
  }

  @Override
  String actionSignature() {
    return "";
  }
}
