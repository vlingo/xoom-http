// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import io.vlingo.xoom.actors.Logger;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.http.Method;
import io.vlingo.xoom.http.Request;
import io.vlingo.xoom.http.Response;

import java.util.Arrays;
import java.util.function.Supplier;

public class RequestHandler8<T, R, U, I, J, K, L, M> extends RequestHandler {
  final ParameterResolver<T> resolverParam1;
  final ParameterResolver<R> resolverParam2;
  final ParameterResolver<U> resolverParam3;
  final ParameterResolver<I> resolverParam4;
  final ParameterResolver<J> resolverParam5;
  final ParameterResolver<K> resolverParam6;
  final ParameterResolver<L> resolverParam7;
  final ParameterResolver<M> resolverParam8;
  private ParamExecutor8<T,R,U,I,J,K,L,M> executor;

  @FunctionalInterface
  public interface Handler8<T, R, U, I, J, K, L, M> {
    Completes<Response> execute(T param1, R param2, U param3, I param4, J param5, K param6, L param7, M param8);
  }

  @FunctionalInterface
  public interface ObjectHandler8<O, T, R, U, I, J, K, L, M> {
    Completes<ObjectResponse<O>> execute(T param1, R param2, U param3, I param4, J param5, K param6, L param7, M param8);
  }

  @FunctionalInterface
  interface ParamExecutor8<T, R, U, I, J, K, L, M> {
    Completes<Response> execute(final Request request,
                                final T param1,
                                final R param2,
                                final U param3,
                                final I param4,
                                final J param5,
                                final K param6,
                                final L param7,
                                final M param8,
                                final MediaTypeMapper mediaTypeMapper,
                                final ErrorHandler errorHandler,
                                final Logger logger);
  }

  RequestHandler8(final Method method,
                  final String path,
                  final ParameterResolver<T> resolverParam1,
                  final ParameterResolver<R> resolverParam2,
                  final ParameterResolver<U> resolverParam3,
                  final ParameterResolver<I> resolverParam4,
                  final ParameterResolver<J> resolverParam5,
                  final ParameterResolver<K> resolverParam6,
                  final ParameterResolver<L> resolverParam7,
                  final ParameterResolver<M> resolverParam8,
                  final ErrorHandler errorHandler,
                  final MediaTypeMapper mediaTypeMapper) {
    super(method, path, Arrays.asList(resolverParam1, resolverParam2, resolverParam3, resolverParam4, resolverParam5, resolverParam6, resolverParam7, resolverParam8), errorHandler, mediaTypeMapper);
    this.resolverParam1 = resolverParam1;
    this.resolverParam2 = resolverParam2;
    this.resolverParam3 = resolverParam3;
    this.resolverParam4 = resolverParam4;
    this.resolverParam5 = resolverParam5;
    this.resolverParam6 = resolverParam6;
    this.resolverParam7 = resolverParam7;
    this.resolverParam8 = resolverParam8;
  }

  Completes<Response> execute(final Request request,
                              final T param1,
                              final R param2,
                              final U param3,
                              final I param4,
                              final J param5,
                              final K param6,
                              final L param7,
                              final M param8,
                              final Logger logger) {
    final Supplier<Completes<Response>> exec = () ->
      executor.execute(request, param1, param2, param3, param4, param5, param6, param7, param8, mediaTypeMapper, errorHandler, logger);

    return runParamExecutor(executor, () -> RequestExecutor.executeRequest(exec, errorHandler, logger));
  }

  public RequestHandler8<T, R, U, I, J, K, L, M> handle(final Handler8<T, R, U, I, J, K, L, M> handler) {
    executor = ((request, param1, param2, param3, param4, param5, param6, param7, param8, mediaTypeMapper1, errorHandler1, logger1) ->
      RequestExecutor.executeRequest(() -> handler.execute(param1, param2, param3, param4, param5, param6, param7, param8), errorHandler1, logger1));
    return this;
  }

  public <O> RequestHandler8<T, R, U, I, J, K, L, M> handle(final ObjectHandler8<O, T, R, U, I, J, K, L, M> handler) {
    executor = (request, param1, param2, param3, param4, param5, param6, param7, param8, mediaTypeMapper1, errorHandler1, logger) ->
      RequestObjectExecutor.executeRequest(request,
        mediaTypeMapper1,
        () -> handler.execute(param1, param2, param3, param4, param5, param6, param7, param8),
        errorHandler1,
        logger);
    return this;
  }

  public RequestHandler8<T, R, U, I, J, K, L, M> onError(final ErrorHandler errorHandler) {
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
    final M param8 = resolverParam8.apply(request, mappedParameters);
    final Supplier<Completes<Response>> exec = () ->
      executor.execute(request, param1, param2, param3, param4, param5, param6, param7, param8, mediaTypeMapper, errorHandler, logger);
    return runParamExecutor(executor, () -> RequestExecutor.executeRequest(exec, errorHandler, logger));
  }

}
