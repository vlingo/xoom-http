// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import io.vlingo.actors.Logger;
import io.vlingo.common.Completes;
import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.Response;

import java.util.Arrays;
import java.util.function.Supplier;

public class RequestHandler6<T, R, U, I, J, K> extends RequestHandler {
  final ParameterResolver<T> resolverParam1;
  final ParameterResolver<R> resolverParam2;
  final ParameterResolver<U> resolverParam3;
  final ParameterResolver<I> resolverParam4;
  final ParameterResolver<J> resolverParam5;
  final ParameterResolver<K> resolverParam6;
  private ParamExecutor6<T,R,U,I,J,K> executor;

  @FunctionalInterface
  public interface Handler6<T, R, U, I, J, K> {
    Completes<Response> execute(T param1, R param2, U param3, I param4, J param5, K param6);
  }

  @FunctionalInterface
  public interface ObjectHandler6<O, T, R, U, I, J, K> {
    Completes<ObjectResponse<O>> execute(T param1, R param2, U param3, I param4, J param5, K param6);
  }

  @FunctionalInterface
  interface ParamExecutor6<T, R, U, I, J, K> {
    Completes<Response> execute(final Request request,
                                final T param1,
                                final R param2,
                                final U param3,
                                final I param4,
                                final J param5,
                                final K param6,
                                final MediaTypeMapper mediaTypeMapper,
                                final ErrorHandler errorHandler,
                                final Logger logger);
  }

  RequestHandler6(final Method method,
                  final String path,
                  final ParameterResolver<T> resolverParam1,
                  final ParameterResolver<R> resolverParam2,
                  final ParameterResolver<U> resolverParam3,
                  final ParameterResolver<I> resolverParam4,
                  final ParameterResolver<J> resolverParam5,
                  final ParameterResolver<K> resolverParam6,
                  final ErrorHandler errorHandler,
                  final MediaTypeMapper mediaTypeMapper) {
    super(method, path, Arrays.asList(resolverParam1, resolverParam2, resolverParam3, resolverParam4, resolverParam5, resolverParam6), errorHandler, mediaTypeMapper);
    this.resolverParam1 = resolverParam1;
    this.resolverParam2 = resolverParam2;
    this.resolverParam3 = resolverParam3;
    this.resolverParam4 = resolverParam4;
    this.resolverParam5 = resolverParam5;
    this.resolverParam6 = resolverParam6;
  }

  Completes<Response> execute(final Request request,
                              final T param1,
                              final R param2,
                              final U param3,
                              final I param4,
                              final J param5,
                              final K param6,
                              final Logger logger) {
    final Supplier<Completes<Response>> exec = () ->
      executor.execute(request, param1, param2, param3, param4, param5, param6, mediaTypeMapper, errorHandler, logger);

    return runParamExecutor(executor, () -> RequestExecutor.executeRequest(exec, errorHandler, logger));
  }

  public RequestHandler6<T, R, U, I, J, K> handle(final Handler6<T, R, U, I, J, K> handler) {
    executor = ((request, param1, param2, param3, param4, param5, param6, mediaTypeMapper1, errorHandler1, logger1) ->
      RequestExecutor.executeRequest(() -> handler.execute(param1, param2, param3, param4, param5, param6), errorHandler1, logger1));
    return this;
  }

  public <O> RequestHandler6<T, R, U, I, J, K> handle(final ObjectHandler6<O, T, R, U, I, J, K> handler) {
    executor = (request, param1, param2, param3, param4, param5, param6, mediaTypeMapper1, errorHandler1, logger) ->
      RequestObjectExecutor.executeRequest(request,
        mediaTypeMapper1,
        () -> handler.execute(param1, param2, param3, param4, param5, param6),
        errorHandler1,
        logger);
    return this;
  }

  public RequestHandler6<T, R, U, I, J, K> onError(final ErrorHandler errorHandler) {
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
    final Supplier<Completes<Response>> exec = () ->
      executor.execute(request, param1, param2, param3, param4, param5, param6, mediaTypeMapper, errorHandler, logger);
    return runParamExecutor(executor, () -> RequestExecutor.executeRequest(exec, errorHandler, logger));
  }

}
