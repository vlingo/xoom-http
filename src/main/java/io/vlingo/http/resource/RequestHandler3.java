// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import io.vlingo.actors.Logger;
import io.vlingo.common.Completes;
import io.vlingo.http.Header;
import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.Response;

import java.util.Arrays;
import java.util.function.Supplier;

public class RequestHandler3<T, R, U> extends RequestHandler {
  final ParameterResolver<T> resolverParam1;
  final ParameterResolver<R> resolverParam2;
  final ParameterResolver<U> resolverParam3;
  private ParamExecutor3<T,R,U> executor;

  @FunctionalInterface
  public interface Handler3<T, R, U> {
    Completes<Response> execute(T param1, R param2, U param3);
  }

  @FunctionalInterface
  public interface ObjectHandler3<O, T, R, U> {
    Completes<ObjectResponse<O>> execute(T param1, R param2, U param3);
  }

  @FunctionalInterface
  interface ParamExecutor3<T, R, U> {
    Completes<Response> execute(final Request request,
                                final T param1,
                                final R param2,
                                final U param3,
                                final MediaTypeMapper mediaTypeMapper,
                                final ErrorHandler errorHandler,
                                final Logger logger);
  }

  RequestHandler3(final Method method,
                  final String path,
                  final ParameterResolver<T> resolverParam1,
                  final ParameterResolver<R> resolverParam2,
                  final ParameterResolver<U> resolverParam3,
                  final ErrorHandler errorHandler,
                  final MediaTypeMapper mediaTypeMapper) {
    super(method, path, Arrays.asList(resolverParam1, resolverParam2, resolverParam3), errorHandler, mediaTypeMapper);
    this.resolverParam1 = resolverParam1;
    this.resolverParam2 = resolverParam2;
    this.resolverParam3 = resolverParam3;
  }

  Completes<Response> execute(final Request request, final T param1, final R param2, final U param3, final Logger logger) {
    final Supplier<Completes<Response>> exec = () ->
      executor.execute(request, param1, param2, param3, mediaTypeMapper, errorHandler, logger);

    return runParamExecutor(executor, () -> RequestExecutor.executeRequest(exec, errorHandler, logger));
  }

  public RequestHandler3<T, R, U> handle(final Handler3<T, R, U> handler) {
    executor = ((request, param1, param2, param3, mediaTypeMapper1, errorHandler1, logger1) ->
      RequestExecutor.executeRequest(() -> handler.execute(param1, param2, param3), errorHandler1, logger1));
    return this;
  }

  public <O> RequestHandler3<T, R, U> handle(final ObjectHandler3<O, T, R, U> handler) {
    executor = ((request, param1, param2, param3, mediaTypeMapper1, errorHandler1, logger) ->
      RequestObjectExecutor.executeRequest(request,
        mediaTypeMapper1,
        () -> handler.execute(param1, param2, param3),
        errorHandler1,
        logger));
    return this;
  }

  public RequestHandler3<T, R, U> onError(final ErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
    return this;
  }

  @Override
  protected Completes<Response> execute(final Request request,
                              final Action.MappedParameters mappedParameters,
                              final Logger logger) {
    final T param1 = resolverParam1.apply(request, mappedParameters);
    final R param2 = resolverParam2.apply(request, mappedParameters);
    final U param3 = resolverParam3.apply(request, mappedParameters);
    final Supplier<Completes<Response>> exec = () ->
      executor.execute(request, param1, param2, param3, mediaTypeMapper, errorHandler, logger);

    return runParamExecutor(executor, () -> RequestExecutor.executeRequest(exec, errorHandler, logger));
  }

  // region FluentAPI
  public <I> RequestHandler4<T, R, U, I> param(final Class<I> paramClass) {
    return new RequestHandler4<>(method, path, resolverParam1, resolverParam2, resolverParam3,
      ParameterResolver.path(3, paramClass),
      errorHandler,
      mediaTypeMapper);
  }

  public <I> RequestHandler4<T, R, U, I> body(final Class<I> bodyClass) {
    return new RequestHandler4<>(method, path, resolverParam1, resolverParam2, resolverParam3,
      ParameterResolver.body(bodyClass, mediaTypeMapper),
      errorHandler,
      mediaTypeMapper);
  }

  /**
   * Specify the class that represents the body of the request for all requests using the specified mapper for all
   * MIME types regardless of the Content-Type header.
   *
   * @deprecated Deprecated in favor of using the ContentMediaType method, which handles media types appropriately.
   * {@link RequestHandler3#body(java.lang.Class, io.vlingo.http.resource.MediaTypeMapper)} instead, or via
   * {@link RequestHandler3#body(java.lang.Class)}
   *
   * @param bodyClass the {@code Class<I>} of the body
   * @param mapperClass the Class of the Mapper
   * @param <I> the body type
   * @return {@code RequestHandler4<T, R, U, I>}
   */
  @Deprecated
  public <I> RequestHandler4<T, R, U, I> body(final Class<I> bodyClass, final Class<? extends Mapper> mapperClass) {
    return body(bodyClass, mapperFrom(mapperClass));
  }

  /**
   * Specify the class that represents the body of the request for all requests using the specified mapper for all
   * MIME types regardless of the Content-Type header.
   *
   * @deprecated Deprecated in favor of using the ContentMediaType method, which handles media types appropriately.
   * {@link RequestHandler3#body(java.lang.Class, io.vlingo.http.resource.MediaTypeMapper)} instead, or via
   * {@link RequestHandler3#body(java.lang.Class)}
   *
   * @param bodyClass the {@code Class<I>} of the body
   * @param mapper the Mapper
   * @param <I> the body type
   * @return {@code RequestHandler4<T, R, U, I>}
   */
  @Deprecated
  public <I> RequestHandler4<T, R, U, I> body(final Class<I> bodyClass, final Mapper mapper) {
    return new RequestHandler4<>(method, path, resolverParam1, resolverParam2, resolverParam3,
      ParameterResolver.body(bodyClass, mapper),
      errorHandler,
      mediaTypeMapper);
  }

  public <I> RequestHandler4<T, R, U, I> body(final Class<I> bodyClass, final MediaTypeMapper mediaTypeMapper) {
    this.mediaTypeMapper = mediaTypeMapper;
    return new RequestHandler4<>(method, path, resolverParam1, resolverParam2, resolverParam3,
      ParameterResolver.body(bodyClass, mediaTypeMapper),
      errorHandler,
      mediaTypeMapper);
  }

  public RequestHandler4<T, R, U, String> query(final String name) {
    return query(name, String.class);
  }

  public <I> RequestHandler4<T, R, U, I> query(final String name, final Class<I> queryClass) {
    return new RequestHandler4<>(method, path, resolverParam1, resolverParam2, resolverParam3,
      ParameterResolver.query(name, queryClass),
      errorHandler,
      mediaTypeMapper);
  }

  public RequestHandler4<T, R, U, Header> header(final String name) {
    return new RequestHandler4<>(method, path, resolverParam1, resolverParam2, resolverParam3,
      ParameterResolver.header(name),
      errorHandler,
      mediaTypeMapper);
  }
  // endregion
}
