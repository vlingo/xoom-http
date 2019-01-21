/*
 * Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.resource;

import io.vlingo.common.Completes;
import io.vlingo.http.Header;
import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.Response;

import java.util.Collections;

public class RequestHandler0 extends RequestHandler {
  private Handler0 handler;

  RequestHandler0(final Method method, final String path) {
    super(method, path, Collections.emptyList());
  }

  Completes<Response> execute() {
    if (handler == null) throw new HandlerMissingException("No handle defined for " + method.toString() + " " + path);
    return handler.execute();
  }

  public RequestHandler0 handle(final Handler0 handler) {
    this.handler = handler;
    return this;
  }

  @Override
  Completes<Response> execute(final Request request,
                              final Action.MappedParameters mappedParameters) {
    return execute();
  }

  @FunctionalInterface
  public interface Handler0 {
    Completes<Response> execute();
  }

  // region FluentAPI
  public <T> RequestHandler1<T> param(final Class<T> paramClass) {
    return new RequestHandler1<>(method, path, ParameterResolver.path(0, paramClass));
  }

  public <T> RequestHandler1<T> body(final Class<T> paramClass) {
    return new RequestHandler1<>(method, path, ParameterResolver.body(paramClass));
  }

  public <T> RequestHandler1<T> body(final Class<T> paramClass, final Class<? extends Mapper> mapperClass ) {
    return body(paramClass, mapperFrom(mapperClass));
  }

  public <T> RequestHandler1<T> body(final Class<T> paramClass, final Mapper mapper ) {
    return new RequestHandler1<>(method, path, ParameterResolver.body(paramClass, mapper));
  }

  public RequestHandler1<String> query(final String name) {
    return query(name, String.class);
  }

  public <T> RequestHandler1<T> query(final String name, final Class<T> type) {
    return query(name, type, null);
  }

  public <T> RequestHandler1<T> query(final String name, final Class<T> type, final T defaultValue) {
    return new RequestHandler1<>(method, path, ParameterResolver.query(name, type, defaultValue));
  }

  public RequestHandler1<Header> header(final String name) {
    return new RequestHandler1<>(method, path, ParameterResolver.header(name));
  }
  // endregion
}
