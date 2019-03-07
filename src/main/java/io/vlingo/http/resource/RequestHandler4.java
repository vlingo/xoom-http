package io.vlingo.http.resource;

import io.vlingo.actors.Logger;
import io.vlingo.common.Completes;
import io.vlingo.http.Header;
import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.Response;

import java.util.Arrays;

public class RequestHandler4<T, R, U, I> extends RequestHandler {
  final ParameterResolver<T> resolverParam1;
  final ParameterResolver<R> resolverParam2;
  final ParameterResolver<U> resolverParam3;
  final ParameterResolver<I> resolverParam4;
  private Handler4<T, R, U, I> handler;
  private ErrorHandler errorHandler;

  RequestHandler4(final Method method,
                  final String path,
                  final ParameterResolver<T> resolverParam1,
                  final ParameterResolver<R> resolverParam2,
                  final ParameterResolver<U> resolverParam3,
                  final ParameterResolver<I> resolverParam4,
                  final ErrorHandler errorHandler) {
    super(method, path, Arrays.asList(resolverParam1, resolverParam2, resolverParam3, resolverParam4));
    this.resolverParam1 = resolverParam1;
    this.resolverParam2 = resolverParam2;
    this.resolverParam3 = resolverParam3;
    this.resolverParam4 = resolverParam4;
    this.errorHandler = errorHandler;
  }

  Completes<Response> execute(final T param1, final R param2, final U param3, final I param4, final Logger logger) {
    checkHandlerOrThrowException(handler);
    return executeRequest(() -> handler.execute(param1, param2, param3, param4), errorHandler, logger);
  }

  public RequestHandler4<T, R, U, I> handle(final Handler4<T, R, U, I> handler) {
    this.handler = handler;
    return this;
  }

  public RequestHandler4<T, R, U, I> onError(final ErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
    return this;
  }

  @Override
  Completes<Response> execute(final Request request,
                              final Action.MappedParameters mappedParameters,
                              final Logger logger) {
    final T param1 = resolverParam1.apply(request, mappedParameters);
    final R param2 = resolverParam2.apply(request, mappedParameters);
    final U param3 = resolverParam3.apply(request, mappedParameters);
    final I param4 = resolverParam4.apply(request, mappedParameters);
    return execute(param1, param2, param3, param4, logger);
  }

  @FunctionalInterface
  public interface Handler4<T, R, U, I> {
    Completes<Response> execute(T param1, R param2, U param3, I param4);
  }

  // region FluentAPI
  public <J> RequestHandler5<T, R, U, I, J> param(final Class<J> paramClass) {
    return new RequestHandler5<>(method, path, resolverParam1, resolverParam2, resolverParam3, resolverParam4,
      ParameterResolver.path(4, paramClass),
      errorHandler);
  }

  public <J> RequestHandler5<T, R, U, I, J> body(final Class<J> bodyClass) {
    return new RequestHandler5<>(method, path, resolverParam1, resolverParam2, resolverParam3, resolverParam4,
      ParameterResolver.body(bodyClass),
      errorHandler);
  }

  public <J> RequestHandler5<T, R, U, I, J> body(final Class<J> bodyClass, final Class<? extends Mapper> mapperClass) {
    return body(bodyClass, mapperFrom(mapperClass));
  }

  public <J> RequestHandler5<T, R, U, I, J> body(final Class<J> bodyClass, final Mapper mapper) {
    return new RequestHandler5<>(method, path, resolverParam1, resolverParam2, resolverParam3, resolverParam4,
      ParameterResolver.body(bodyClass, mapper),
      errorHandler);
  }

  public RequestHandler5<T, R, U, I, String> query(final String name) {
    return query(name, String.class);
  }

  public <J> RequestHandler5<T, R, U, I, J> query(final String name, final Class<J> queryClass) {
    return new RequestHandler5<>(method, path, resolverParam1, resolverParam2, resolverParam3, resolverParam4,
      ParameterResolver.query(name, queryClass),
      errorHandler);
  }

  public RequestHandler5<T, R, U, I, Header> header(final String name) {
    return new RequestHandler5<>(method, path, resolverParam1, resolverParam2, resolverParam3, resolverParam4,
      ParameterResolver.header(name),
      errorHandler);
  }
  // endregion
}
