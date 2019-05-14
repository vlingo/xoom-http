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
  private ObjectHandler4<T, R, U, I> objectHandler;

  private ErrorHandler errorHandler;
  private MediaTypeMapper mediaTypeMapper;

  RequestHandler4(final Method method,
                  final String path,
                  final ParameterResolver<T> resolverParam1,
                  final ParameterResolver<R> resolverParam2,
                  final ParameterResolver<U> resolverParam3,
                  final ParameterResolver<I> resolverParam4,
                  final ErrorHandler errorHandler,
                  final MediaTypeMapper mediaTypeMapper) {
    super(method, path, Arrays.asList(resolverParam1, resolverParam2, resolverParam3, resolverParam4));
    this.resolverParam1 = resolverParam1;
    this.resolverParam2 = resolverParam2;
    this.resolverParam3 = resolverParam3;
    this.resolverParam4 = resolverParam4;
    this.errorHandler = errorHandler;
    this.mediaTypeMapper = mediaTypeMapper;
  }

  Completes<Response> execute(final Request request,
                              final T param1,
                              final R param2,
                              final U param3,
                              final I param4,
                              final Logger logger) {
    checkHandlerOrThrowException(handler, objectHandler);
    if (handler != null) {
      return executeRequest(() -> handler.execute(param1, param2, param3, param4), errorHandler, logger);
    } else {
      return executeObjectRequest(request,
        mediaTypeMapper,
        () -> objectHandler.execute(param1, param2, param3, param4),
        errorHandler,
        logger);
    }
  }

  public RequestHandler4<T, R, U, I> handle(final Handler4<T, R, U, I> handler) {
    if (this.objectHandler != null) {
      throw new IllegalArgumentException("Handler already specified via .handle(...)");
    }
    this.handler = handler;
    return this;
  }

  public RequestHandler4<T, R, U, I> handle(final ObjectHandler4<T, R, U, I> handler) {
    if (this.handler != null) {
      throw new IllegalArgumentException("Handler already specified via .handle(...)");
    }
    this.objectHandler = handler;
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
    return execute(request, param1, param2, param3, param4, logger);
  }

  @FunctionalInterface
  public interface Handler4<T, R, U, I> {
    Completes<Response> execute(T param1, R param2, U param3, I param4);
  }

  @FunctionalInterface
  public interface ObjectHandler4<T, R, U, I> {
    Completes<ObjectResponse<?>> execute(T param1, R param2, U param3, I param4);
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
   * {@link RequestHandler4#body(java.lang.Class, io.vlingo.http.resource.MediaTypeMapper)} instead, or via
   * {@link RequestHandler4#body(java.lang.Class)}
   */
  public <J> RequestHandler5<T, R, U, I, J> body(final Class<J> bodyClass, final Class<? extends Mapper> mapperClass) {
    return body(bodyClass, mapperFrom(mapperClass));
  }

  /**
   * Specify the class that represents the body of the request for all requests using the specified mapper for all
   * MIME types regardless of the Content-Type header.
   *
   * @deprecated Deprecated in favor of using the ContentMediaType method, which handles media types appropriately.
   * {@link RequestHandler4#body(java.lang.Class, io.vlingo.http.resource.MediaTypeMapper)} instead, or via
   * {@link RequestHandler4#body(java.lang.Class)}
   */
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
