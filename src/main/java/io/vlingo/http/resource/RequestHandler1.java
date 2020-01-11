/*
 * Copyright © 2012-2020 VLINGO LABS. All rights reserved.
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
import java.util.function.Supplier;

public class RequestHandler1<T> extends RequestHandler {

  @FunctionalInterface
  public interface Handler1<T> {
    Completes<Response> execute(T param1);
  }

  @FunctionalInterface
  public interface ObjectHandler1<O, T> {
    Completes<ObjectResponse<O>> execute(T param1);
  }

  @FunctionalInterface
  interface ParamExecutor1<T> {
    Completes<Response> execute(final Request request,
                                final T param1,
                                final MediaTypeMapper mediaTypeMapper,
                                final ErrorHandler errorHandler,
                                final Logger logger);
  }

  final ParameterResolver<T> resolver;
  private ParamExecutor1<T> executor;

  RequestHandler1(final Method method,
                  final String path,
                  final ParameterResolver<T> resolver,
                  final ErrorHandler errorHandler,
                  final MediaTypeMapper mediaTypeMapper) {
    super(method, path, Collections.singletonList(resolver), errorHandler, mediaTypeMapper);
    this.resolver = resolver;
  }

  public RequestHandler1<T> handle(final Handler1<T> handler) {
    executor = ((request, param1, mediaTypeMapper1, errorHandler1, logger1) ->
      RequestExecutor.executeRequest(() -> handler.execute(param1), errorHandler1, logger1));
    return this;
  }

  public <O> RequestHandler1<T> handle(final RequestHandler1.ObjectHandler1<O, T> handler) {
    executor = ((request, param1, mediaTypeMapper1, errorHandler1, logger) ->
      RequestObjectExecutor.executeRequest(request,
                                           mediaTypeMapper1,
                                           () -> handler.execute(param1),
                                           errorHandler1,
                                           logger));
    return this;
  }

  public RequestHandler1<T> onError(ErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
    return this;
  }

  Completes<Response> execute(final Request request, final T param1, final Logger logger) {
    final Supplier<Completes<Response>> exec = () ->
      executor.execute(request, param1, mediaTypeMapper, errorHandler, logger);

    return runParamExecutor(executor, () -> RequestExecutor.executeRequest(exec, errorHandler, logger));
  }

  @Override
  protected Completes<Response> execute(final Request request,
                              final Action.MappedParameters mappedParameters,
                              final Logger logger) {
    return execute(request, resolver.apply(request, mappedParameters), logger);
  }

  // region FluentAPI

  public <R> RequestHandler2<T, R> param(final Class<R> paramClass) {
    return new RequestHandler2<>(method, path, resolver, ParameterResolver.path(1, paramClass), errorHandler,
                                 mediaTypeMapper);
  }

  public <R> RequestHandler2<T, R> body(final Class<R> bodyClass) {
    return new RequestHandler2<>(method, path, resolver, ParameterResolver.body(bodyClass, mediaTypeMapper),
                                 errorHandler, mediaTypeMapper);
  }

  /**
   * Specify the class that represents the body of the request for all requests using the specified mapper for all
   * MIME types regardless of the Content-Type header.
   *
   * @deprecated Deprecated in favor of using the ContentMediaType method, which handles media types appropriately.
   * {@link RequestHandler1#body(java.lang.Class, io.vlingo.http.resource.MediaTypeMapper)} instead, or via
   * {@link RequestHandler1#body(java.lang.Class)}
   *
   * @param bodyClass the R typed {@code Class<R>} of the parameter
   * @param mapperClass the Mapper
   * @param <R> the body type
   * @return {@code RequestHandler2<T, R>}
   */
  @Deprecated
  public <R> RequestHandler2<T, R> body(final Class<R> bodyClass, final Class<? extends Mapper> mapperClass) {
    return body(bodyClass, mapperFrom(mapperClass));
  }

  /**
   * Specify the class that represents the body of the request for all requests using the specified mapper for all
   * MIME types regardless of the Content-Type header.
   *
   * @deprecated Deprecated in favor of using the ContentMediaType method, which handles media types appropriately.
   * {@link RequestHandler1#body(java.lang.Class, io.vlingo.http.resource.MediaTypeMapper)}instead, or via
   * {@link RequestHandler1#body(java.lang.Class)}
   *
   * @param bodyClass the R typed {@code Class<R>} of the parameter
   * @param mapper the Mapper
   * @param <R> the body type
   * @return {@code RequestHandler2<T,R>}
   */
  @Deprecated
  public <R> RequestHandler2<T, R> body(final Class<R> bodyClass, final Mapper mapper) {
    return new RequestHandler2<>(method, path, resolver, ParameterResolver.body(bodyClass, mapper), errorHandler,
                                 mediaTypeMapper);
  }

  public <R> RequestHandler2<T, R> body(final Class<R> bodyClass, final MediaTypeMapper mediaTypeMapper) {
    this.mediaTypeMapper = mediaTypeMapper;
    return new RequestHandler2<>(method, path, resolver, ParameterResolver.body(bodyClass, mediaTypeMapper),
                                 errorHandler, mediaTypeMapper);
  }

  public RequestHandler2<T, String> query(final String name) {
    return query(name, String.class);
  }

  public <R> RequestHandler2<T, R> query(final String name, final Class<R> queryClass) {
    return query(name, queryClass, null);
  }

  public <R> RequestHandler2<T, R> query(final String name, final Class<R> queryClass, final R defaultValue) {
    return new RequestHandler2<>(method, path, resolver, ParameterResolver.query(name, queryClass, defaultValue),
                                 errorHandler, mediaTypeMapper);
  }

  public RequestHandler2<T, Header> header(final String name) {
    return new RequestHandler2<>(method, path, resolver, ParameterResolver.header(name), errorHandler, mediaTypeMapper);
  }

  // endregion
}
