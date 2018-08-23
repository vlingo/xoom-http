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

import java.lang.reflect.InvocationTargetException;

public class RequestHandler2<T, R> implements RequestHandler {
  private final Method method;
  private final String path;
  private final Class<T> param1Class;
  private final Class<R> param2Class;
  private Handler2<T,R> handler;

  RequestHandler2(final Method method, final String path, final Class<T> param1, final Class<R> param2) {
    this.method = method;
    this.path = path;
    this.param1Class = param1;
    this.param2Class = param2;
  }

  @FunctionalInterface
  interface Handler2<T,R> {
    Response execute(T param1, R param2);
  }

  public RequestHandler2<T,R> handle(final Handler2<T,R> handler) {
    this.handler = handler;
    return this;
  }

  Response execute(final T param1, final R param2) {
    if(this.handler == null) throw new HandlerMissingException("No handle defined for " + method.toString() + " " + path);
    return this.handler.execute(param1, param2);
  }

  @Override
  public Response execute(Request request, Action.MappedParameters mappedParameters) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    final T param1 = this.param1Class.getConstructor(this.param1Class).newInstance(mappedParameters.mapped.get(0));
    final R param2 = this.param2Class.getConstructor(this.param2Class).newInstance(mappedParameters.mapped.get(1));
    return this.execute(param1, param2);
  }

  @Override
  public Method method() {
    return this.method;
  }

  @Override
  public String path() {
    return this.path;
  }
}
