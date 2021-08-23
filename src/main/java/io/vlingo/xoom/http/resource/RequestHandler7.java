// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
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

public class RequestHandler7<T, R, U, I, J, K, L> extends RequestHandler {
  final ParameterResolver<T> resolverParam1;
  final ParameterResolver<R> resolverParam2;
  final ParameterResolver<U> resolverParam3;
  final ParameterResolver<I> resolverParam4;
  final ParameterResolver<J> resolverParam5;
  final ParameterResolver<K> resolverParam6;
  final ParameterResolver<L> resolverParam7;
  private ParamExecutor7<T,R,U,I,J,K, L> executor;

  @FunctionalInterface
  public interface Handler7<T, R, U, I, J, K, L> {
    Completes<Response> execute(T param1, R param2, U param3, I param4, J param5, K param6, L param7);
  }

  @FunctionalInterface
  public interface ObjectHandler7<O, T, R, U, I, J, K, L> {
    Completes<ObjectResponse<O>> execute(T param1, R param2, U param3, I param4, J param5, K param6, L param7);
  }

  @FunctionalInterface
  interface ParamExecutor7<T, R, U, I, J, K, L> {
    Completes<Response> execute(final Request request,
                                final T param1,
                                final R param2,
                                final U param3,
                                final I param4,
                                final J param5,
                                final K param6,
                                final L param7,
                                final MediaTypeMapper mediaTypeMapper,
                                final ErrorHandler errorHandler,
                                final Logger logger);
  }

  RequestHandler7(final Method method,
                  final String path,
                  final ParameterResolver<T> resolverParam1,
                  final ParameterResolver<R> resolverParam2,
                  final ParameterResolver<U> resolverParam3,
                  final ParameterResolver<I> resolverParam4,
                  final ParameterResolver<J> resolverParam5,
                  final ParameterResolver<K> resolverParam6,
                  final ParameterResolver<L> resolverParam7,
                  final ErrorHandler errorHandler,
                  final MediaTypeMapper mediaTypeMapper) {
    super(method, path, Arrays.asList(resolverParam1, resolverParam2, resolverParam3, resolverParam4, resolverParam5, resolverParam6, resolverParam7), errorHandler, mediaTypeMapper);
    this.resolverParam1 = resolverParam1;
    this.resolverParam2 = resolverParam2;
    this.resolverParam3 = resolverParam3;
    this.resolverParam4 = resolverParam4;
    this.resolverParam5 = resolverParam5;
    this.resolverParam6 = resolverParam6;
    this.resolverParam7 = resolverParam7;
  }

  Completes<Response> execute(final Request request,
                              final T param1,
                              final R param2,
                              final U param3,
                              final I param4,
                              final J param5,
                              final K param6,
                              final L param7,
                              final Logger logger) {
    final Supplier<Completes<Response>> exec = () ->
      executor.execute(request, param1, param2, param3, param4, param5, param6, param7, mediaTypeMapper, errorHandler, logger);

    return runParamExecutor(executor, () -> RequestExecutor.executeRequest(exec, errorHandler, logger));
  }

  public RequestHandler7<T, R, U, I, J, K, L> handle(final Handler7<T, R, U, I, J, K, L> handler) {
    executor = ((request, param1, param2, param3, param4, param5, param6, param7, mediaTypeMapper1, errorHandler1, logger1) ->
      RequestExecutor.executeRequest(() -> handler.execute(param1, param2, param3, param4, param5, param6, param7), errorHandler1, logger1));
    return this;
  }

  public <O> RequestHandler7<T, R, U, I, J, K, L> handle(final ObjectHandler7<O, T, R, U, I, J, K, L> handler) {
    executor = (request, param1, param2, param3, param4, param5, param6, param7, mediaTypeMapper1, errorHandler1, logger) ->
      RequestObjectExecutor.executeRequest(request,
        mediaTypeMapper1,
        () -> handler.execute(param1, param2, param3, param4, param5, param6, param7),
        errorHandler1,
        logger);
    return this;
  }

  public RequestHandler7<T, R, U, I, J, K, L> onError(final ErrorHandler errorHandler) {
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
    final J param5 = resolverParam5.apply(request, mappedParameters);
    final K param6 = resolverParam6.apply(request, mappedParameters);
    final L param7 = resolverParam7.apply(request, mappedParameters);
    final Supplier<Completes<Response>> exec = () ->
      executor.execute(request, param1, param2, param3, param4, param5, param6, param7, mediaTypeMapper, errorHandler, logger);
    return runParamExecutor(executor, () -> RequestExecutor.executeRequest(exec, errorHandler, logger));
  }

  public <M> RequestHandler8<T, R, U, I, J, K, L, M> param(final Class<M> paramClass) {
    return new RequestHandler8<>(method, path, resolverParam1, resolverParam2, resolverParam3, resolverParam4, resolverParam5, resolverParam6, resolverParam7,
      ParameterResolver.path(7, paramClass),
      errorHandler,
      mediaTypeMapper);
  }

  public <M> RequestHandler8<T, R, U, I, J, K, L, M> body(final Class<M> bodyClass) {
    return new RequestHandler8<>(method, path, resolverParam1, resolverParam2, resolverParam3, resolverParam4, resolverParam5, resolverParam6, resolverParam7,
      ParameterResolver.body(bodyClass, mediaTypeMapper),
      errorHandler,
      mediaTypeMapper);
  }

  public RequestHandler8<T, R, U, I, J, K, L, String> query(final String name) {
    return query(name, String.class);
  }

  public <M> RequestHandler8<T, R, U, I, J, K, L, M> query(final String name, final Class<M> queryClass) {
    return new RequestHandler8<>(method, path, resolverParam1, resolverParam2, resolverParam3, resolverParam4, resolverParam5, resolverParam6, resolverParam7,
      ParameterResolver.query(name, queryClass),
      errorHandler,
      mediaTypeMapper);
  }

  public RequestHandler8<T, R, U, I, J, K, L, Header> header(final String name) {
    return new RequestHandler8<>(method, path, resolverParam1, resolverParam2, resolverParam3, resolverParam4, resolverParam5, resolverParam6, resolverParam7,
      ParameterResolver.header(name),
      errorHandler,
      mediaTypeMapper);
  }

}
