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

public class RequestHandler1<T> extends RequestHandler {
  final ParameterResolver<T> resolver;
  private Handler1<T> handler;

  RequestHandler1(final Method method, final String path, final ParameterResolver<T> resolver) {
    super(method, path, Collections.singletonList(resolver));
    this.resolver = resolver;
  }

  Completes execute(final T param1) {
    if (handler == null)
      throw new HandlerMissingException("No handle defined for " + method.toString() + " " + path);
    return handler.execute(param1);
  }

  public RequestHandler1<T> handle(final Handler1<T> handler) {
    this.handler = handler;
    return this;
  }

  @Override
  Completes execute(final Request request,
               final Action.MappedParameters mappedParameters) {
    return execute(resolver.apply(request, mappedParameters));
  }

  @FunctionalInterface
  public interface Handler1<T> {
    Completes execute(T param1);
  }

  // region FluentAPI
  public <R> RequestHandler2<T, R> param(final Class<R> paramClass) {
    return new RequestHandler2<>(method, path, resolver, ParameterResolver.path(1, paramClass));
  }

  public <R> RequestHandler2<T, R> body(final Class<R> bodyClass) {
    return new RequestHandler2<>(method, path, resolver, ParameterResolver.body(bodyClass));
  }

  public RequestHandler2<T, String> query(final String name) {
    return query(name, String.class);
  }

  public <R> RequestHandler2<T, R> query(final String name, final Class<R> queryClass) {
    return new RequestHandler2<>(method, path, resolver, ParameterResolver.query(name, queryClass));
  }

  public RequestHandler2<T, Header> header(final String name) {
    return new RequestHandler2<>(method, path, resolver, ParameterResolver.header(name));
  }
  // endregion
}
