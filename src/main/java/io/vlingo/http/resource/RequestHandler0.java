/*
 * Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.resource;

import io.vlingo.actors.Completes;
import io.vlingo.http.Header;
import io.vlingo.http.Method;
import io.vlingo.http.Request;

import java.util.Collections;

public class RequestHandler0 extends RequestHandler {
  private Handler0 handler;

  RequestHandler0(final Method method, final String path) {
    super(method, path, Collections.emptyList());
  }

  Completes execute() {
    if (handler == null) throw new HandlerMissingException("No handle defined for " + method.toString() + " " + path);
    return handler.execute();
  }

  public RequestHandler0 handle(final Handler0 handler) {
    this.handler = handler;
    return this;
  }

  @Override
  Completes execute(final Request request,
                   final Action.MappedParameters mappedParameters) {
    return execute();
  }

  @FunctionalInterface
  public interface Handler0 {
    Completes execute();
  }

  // region FluentAPI
  public <T> RequestHandler1<T> param(final Class<T> paramClass) {
    return new RequestHandler1<>(method, path, ParameterResolver.path(0, paramClass));
  }

  public <T> RequestHandler1<T> body(final Class<T> paramClass) {
    return new RequestHandler1<>(method, path, ParameterResolver.body(paramClass));
  }

  public RequestHandler1<String> query(final String name) {
    return query(name, String.class);
  }

  public <T> RequestHandler1<T> query(final String name, final Class<T> queryClass) {
    return new RequestHandler1<>(method, path, ParameterResolver.query(name, queryClass));
  }

  public RequestHandler1<Header> header(final String name) {
    return new RequestHandler1<>(method, path, ParameterResolver.header(name));
  }
  // endregion
}
