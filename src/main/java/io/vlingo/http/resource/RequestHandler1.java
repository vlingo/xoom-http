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

public class RequestHandler1<T> implements RequestHandler {
  private final Method method;
  private final String path;
  final Class<T> param1Class;
  private Handler1<T> handler;

  RequestHandler1(final Method method, final String path, final Class<T> param1) {
    this.method = method;
    this.path = path;
    this.param1Class = param1;
  }

  public <R> RequestHandler2<T,R> body(final Class<R> bodyClass) {
    return new RequestHandler2<>(this.method, this.path, this.param1Class, bodyClass);
  }

  @FunctionalInterface
  public interface Handler1<T> {
    Response execute(T param1);
  }

  public RequestHandler1<T> handle(final Handler1<T> handler) {
    this.handler = handler;
    return this;
  }

  Response execute(final T param1) {
    if(this.handler == null) throw new HandlerMissingException("No handle defined for " + method.toString() + " " + path);
    return this.handler.execute(param1);
  }

  @Override
  public Response execute(Request request, Action.MappedParameters mappedParameters) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    final T param1 = this.param1Class.getConstructor(this.param1Class).newInstance(mappedParameters.mapped.get(0));
    return this.execute(param1);
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
