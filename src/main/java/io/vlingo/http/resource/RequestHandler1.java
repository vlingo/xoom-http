/*
 * Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.resource;

import io.vlingo.actors.Logger;
import io.vlingo.common.Completes;
import io.vlingo.http.Header;
import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.Response;

import java.util.Collections;

public class RequestHandler1<T> extends RequestHandler {
  final ParameterResolver<T> resolver;
  private Handler1<T> handler;
  private ErrorHandler errorHandler;

  RequestHandler1(final Method method, final String path, final ParameterResolver<T> resolver,
                  final ErrorHandler errorHandler) {
    super(method, path, Collections.singletonList(resolver));
    this.resolver = resolver;
    this.errorHandler = errorHandler;
  }

  Completes<Response> execute(final T param1, final Logger logger) {
    checkHandlerOrThrowException(handler);
    return executeRequest(() -> handler.execute(param1), errorHandler, logger);
  }

  public RequestHandler1<T> handle(final Handler1<T> handler) {
    this.handler = handler;
    return this;
  }

  public RequestHandler1<T> onError(ErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
    return this;
  }

  @Override
  Completes<Response> execute(final Request request,
                              final Action.MappedParameters mappedParameters,
                              final Logger logger) {
    return execute(resolver.apply(request, mappedParameters), logger);
  }

  @FunctionalInterface
  public interface Handler1<T> {
    Completes<Response> execute(T param1);
  }

  // region FluentAPI

  public <R> RequestHandler2<T, R> param(final Class<R> paramClass) {
    return new RequestHandler2<>(method, path, resolver, ParameterResolver.path(1, paramClass), errorHandler);
  }

  public <R> RequestHandler2<T, R> body(final Class<R> bodyClass) {
    return new RequestHandler2<>(method, path, resolver, ParameterResolver.body(bodyClass), errorHandler);
  }

  public <R> RequestHandler2<T, R> body(final Class<R> bodyClass, final Class<? extends Mapper> mapperClass) {
    return body(bodyClass, mapperFrom(mapperClass));
  }

  public <R> RequestHandler2<T, R> body(final Class<R> bodyClass, final Mapper mapper) {
    return new RequestHandler2<>(method, path, resolver, ParameterResolver.body(bodyClass, mapper), errorHandler);
  }

  public RequestHandler2<T, String> query(final String name) {
    return query(name, String.class);
  }

  public <R> RequestHandler2<T, R> query(final String name, final Class<R> queryClass) {
    return query(name, queryClass, null);
  }

  public <R> RequestHandler2<T, R> query(final String name, final Class<R> queryClass, final R defaultValue) {
    return new RequestHandler2<>(method, path, resolver, ParameterResolver.query(name, queryClass, defaultValue), errorHandler);
  }

  public RequestHandler2<T, Header> header(final String name) {
    return new RequestHandler2<>(method, path, resolver, ParameterResolver.header(name), errorHandler);
  }

  // endregion
}
