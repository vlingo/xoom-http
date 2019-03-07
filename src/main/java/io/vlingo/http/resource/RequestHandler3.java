package io.vlingo.http.resource;

import io.vlingo.actors.Logger;
import io.vlingo.common.Completes;
import io.vlingo.http.Header;
import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.Response;

import java.util.Arrays;

public class RequestHandler3<T, R, U> extends RequestHandler {
  final ParameterResolver<T> resolverParam1;
  final ParameterResolver<R> resolverParam2;
  final ParameterResolver<U> resolverParam3;
  private Handler3<T, R, U> handler;
  private ErrorHandler errorHandler;

  RequestHandler3(final Method method,
                  final String path,
                  final ParameterResolver<T> resolverParam1,
                  final ParameterResolver<R> resolverParam2,
                  final ParameterResolver<U> resolverParam3,
                  final ErrorHandler errorHandler) {
    super(method, path, Arrays.asList(resolverParam1, resolverParam2, resolverParam3));
    this.resolverParam1 = resolverParam1;
    this.resolverParam2 = resolverParam2;
    this.resolverParam3 = resolverParam3;
    this.errorHandler = errorHandler;
  }

  Completes<Response> execute(final T param1, final R param2, final U param3, final Logger logger) {
    checkHandlerOrThrowException(handler);
    return executeRequest(() -> handler.execute(param1, param2, param3), errorHandler, logger);
  }

  public RequestHandler3<T, R, U> handle(final Handler3<T, R, U> handler) {
    this.handler = handler;
    return this;
  }

  public RequestHandler3<T, R, U> onError(final ErrorHandler errorHandler) {
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
    return execute(param1, param2, param3, logger);
  }

  @FunctionalInterface
  public interface Handler3<T, R, U> {
    Completes<Response> execute(T param1, R param2, U param3);
  }

  // region FluentAPI
  public <I> RequestHandler4<T, R, U, I> param(final Class<I> paramClass) {
    return new RequestHandler4<>(method, path, resolverParam1, resolverParam2, resolverParam3,
      ParameterResolver.path(3, paramClass),
      errorHandler);
  }

  public <I> RequestHandler4<T, R, U, I> body(final Class<I> bodyClass) {
    return new RequestHandler4<>(method, path, resolverParam1, resolverParam2, resolverParam3,
      ParameterResolver.body(bodyClass),
      errorHandler);
  }

  public <I> RequestHandler4<T, R, U, I> body(final Class<I> bodyClass, final Class<? extends Mapper> mapperClass) {
    return body(bodyClass, mapperFrom(mapperClass));
  }

  public <I> RequestHandler4<T, R, U, I> body(final Class<I> bodyClass, final Mapper mapper) {
    return new RequestHandler4<>(method, path, resolverParam1, resolverParam2, resolverParam3,
      ParameterResolver.body(bodyClass, mapper),
      errorHandler);
  }

  public RequestHandler4<T, R, U, String> query(final String name) {
    return query(name, String.class);
  }

  public <I> RequestHandler4<T, R, U, I> query(final String name, final Class<I> queryClass) {
    return new RequestHandler4<>(method, path, resolverParam1, resolverParam2, resolverParam3,
      ParameterResolver.query(name, queryClass),
      errorHandler);
  }

  public RequestHandler4<T, R, U, Header> header(final String name) {
    return new RequestHandler4<>(method, path, resolverParam1, resolverParam2, resolverParam3,
      ParameterResolver.header(name),
      errorHandler);
  }
  // endregion
}
