// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import io.vlingo.xoom.actors.Logger;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.http.Header;
import io.vlingo.xoom.http.Method;
import io.vlingo.xoom.http.Request;
import io.vlingo.xoom.http.Response;

import java.util.Arrays;
import java.util.function.Supplier;

public class RequestHandler4<T, R, U, I> extends RequestHandler {
  final ParameterResolver<T> resolverParam1;
  final ParameterResolver<R> resolverParam2;
  final ParameterResolver<U> resolverParam3;
  final ParameterResolver<I> resolverParam4;
  private ParamExecutor4<T,R,U,I> executor;


  @FunctionalInterface
  public interface Handler4<T, R, U, I> {
    Completes<Response> execute(T param1, R param2, U param3, I param4);
  }

  @FunctionalInterface
  public interface ObjectHandler4<O, T, R, U, I> {
    Completes<ObjectResponse<O>> execute(T param1, R param2, U param3, I param4);
  }

  interface ParamExecutor4<T, R, U, I> {
    Completes<Response> execute(final Request request,
                                final T param1,
                                final R param2,
                                final U param3,
                                final I param4,
                                final MediaTypeMapper mediaTypeMapper,
                                final ErrorHandler errorHandler,
                                final Logger logger);
  }

  RequestHandler4(final Method method,
                  final String path,
                  final ParameterResolver<T> resolverParam1,
                  final ParameterResolver<R> resolverParam2,
                  final ParameterResolver<U> resolverParam3,
                  final ParameterResolver<I> resolverParam4,
                  final ErrorHandler errorHandler,
                  final MediaTypeMapper mediaTypeMapper) {
    super(method, path, Arrays.asList(resolverParam1, resolverParam2, resolverParam3, resolverParam4), errorHandler, mediaTypeMapper);
    this.resolverParam1 = resolverParam1;
    this.resolverParam2 = resolverParam2;
    this.resolverParam3 = resolverParam3;
    this.resolverParam4 = resolverParam4;
  }

  Completes<Response> execute(final Request request,
                              final T param1,
                              final R param2,
                              final U param3,
                              final I param4,
                              final Logger logger) {
    final Supplier<Completes<Response>> exec = () ->
      executor.execute(request, param1, param2, param3, param4, mediaTypeMapper, errorHandler, logger);

    return runParamExecutor(executor, () -> RequestExecutor.executeRequest(exec, errorHandler, logger));
  }

  public RequestHandler4<T, R, U, I> handle(final Handler4<T, R, U, I> handler) {
    executor = ((request, param1, param2, param3, param4, mediaTypeMapper1, errorHandler1, logger1) ->
      RequestExecutor.executeRequest(() -> handler.execute(param1, param2, param3, param4), errorHandler1, logger1));
    return this;
  }

  public <O> RequestHandler4<T, R, U, I> handle(final ObjectHandler4<O, T, R, U, I> handler) {
    executor = ((request, param1, param2, param3, param4, mediaTypeMapper1, errorHandler1, logger) ->
      RequestObjectExecutor.executeRequest(request,
        mediaTypeMapper1,
        () -> handler.execute(param1, param2, param3, param4),
        errorHandler1,
        logger));
    return this;
  }

  public RequestHandler4<T, R, U, I> onError(final ErrorHandler errorHandler) {
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
    final I param4 = resolverParam4.apply(request, mappedParameters);
    final Supplier<Completes<Response>> exec = () ->
      executor.execute(request, param1, param2, param3, param4, mediaTypeMapper, errorHandler, logger);
    return runParamExecutor(executor, () -> RequestExecutor.executeRequest(exec, errorHandler, logger));
  }


  // region FluentAPI
  public <J> RequestHandler5<T, R, U, I, J> param(final Class<J> paramClass) {
    return new RequestHandler5<>(method, path, resolverParam1, resolverParam2, resolverParam3, resolverParam4,
      ParameterResolver.path(4, paramClass),
      errorHandler,
      mediaTypeMapper);
  }

  public <J> RequestHandler5<T, R, U, I, J> body(final Class<J> bodyClass) {
    return new RequestHandler5<>(method, path, resolverParam1, resolverParam2, resolverParam3, resolverParam4,
      ParameterResolver.body(bodyClass, mediaTypeMapper),
      errorHandler,
      mediaTypeMapper);
  }

  /**
   * Specify the class that represents the body of the request for all requests using the specified mapper for all
   * MIME types regardless of the Content-Type header.
   *
   * @deprecated Deprecated in favor of using the ContentMediaType method, which handles media types appropriately.
   * {@link RequestHandler4#body(java.lang.Class, io.vlingo.xoom.http.resource.MediaTypeMapper)} instead, or via
   * {@link RequestHandler4#body(java.lang.Class)}
   *
   * @param bodyClass the {@code Class<I>} of the body
   * @param mapperClass the Class of the Mapper
   * @param <J> the body type
   * @return {@code RequestHandler5<T, R, U, I, J>}
   */
  @Deprecated
  public <J> RequestHandler5<T, R, U, I, J> body(final Class<J> bodyClass, final Class<? extends Mapper> mapperClass) {
    return body(bodyClass, mapperFrom(mapperClass));
  }

  /**
   * Specify the class that represents the body of the request for all requests using the specified mapper for all
   * MIME types regardless of the Content-Type header.
   *
   * @deprecated Deprecated in favor of using the ContentMediaType method, which handles media types appropriately.
   * {@link RequestHandler4#body(java.lang.Class, io.vlingo.xoom.http.resource.MediaTypeMapper)} instead, or via
   * {@link RequestHandler4#body(java.lang.Class)}
   *
   * @param bodyClass the {@code Class<I>} of the body
   * @param mapper the Mapper
   * @param <J> the body type
   * @return {@code RequestHandler5<T, R, U, I, J>}
   */
  @Deprecated
  public <J> RequestHandler5<T, R, U, I, J> body(final Class<J> bodyClass, final Mapper mapper) {
    return new RequestHandler5<>(method, path, resolverParam1, resolverParam2, resolverParam3, resolverParam4,
      ParameterResolver.body(bodyClass, mapper),
      errorHandler,
      mediaTypeMapper);
  }

  public <J> RequestHandler5<T, R, U, I, J> body(final Class<J> bodyClass, final MediaTypeMapper mediaTypeMapper) {
    return new RequestHandler5<>(method, path, resolverParam1, resolverParam2, resolverParam3, resolverParam4,
      ParameterResolver.body(bodyClass, mediaTypeMapper),
      errorHandler,
      mediaTypeMapper);
  }

  public RequestHandler5<T, R, U, I, String> query(final String name) {
    return query(name, String.class);
  }

  public <J> RequestHandler5<T, R, U, I, J> query(final String name, final Class<J> queryClass) {
    return new RequestHandler5<>(method, path, resolverParam1, resolverParam2, resolverParam3, resolverParam4,
      ParameterResolver.query(name, queryClass),
      errorHandler,
      mediaTypeMapper);
  }

  public RequestHandler5<T, R, U, I, Header> header(final String name) {
    return new RequestHandler5<>(method, path, resolverParam1, resolverParam2, resolverParam3, resolverParam4,
      ParameterResolver.header(name),
      errorHandler,
      mediaTypeMapper);
  }
  // endregion
}
