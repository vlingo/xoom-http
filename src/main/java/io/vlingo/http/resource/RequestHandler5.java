package io.vlingo.http.resource;

import io.vlingo.actors.Logger;
import io.vlingo.common.Completes;
import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.Response;

import java.util.Arrays;

public class RequestHandler5<T, R, U, I, J> extends RequestHandler {
  final ParameterResolver<T> resolverParam1;
  final ParameterResolver<R> resolverParam2;
  final ParameterResolver<U> resolverParam3;
  final ParameterResolver<I> resolverParam4;
  final ParameterResolver<J> resolverParam5;
  private ParamExecutor5<T,R,U,I,J> executor;

  RequestHandler5(final Method method,
                  final String path,
                  final ParameterResolver<T> resolverParam1,
                  final ParameterResolver<R> resolverParam2,
                  final ParameterResolver<U> resolverParam3,
                  final ParameterResolver<I> resolverParam4,
                  final ParameterResolver<J> resolverParam5,
                  final ErrorHandler errorHandler,
                  final MediaTypeMapper mediaTypeMapper) {
    super(method, path, Arrays.asList(resolverParam1, resolverParam2, resolverParam3, resolverParam4, resolverParam5), errorHandler, mediaTypeMapper);
    this.resolverParam1 = resolverParam1;
    this.resolverParam2 = resolverParam2;
    this.resolverParam3 = resolverParam3;
    this.resolverParam4 = resolverParam4;
    this.resolverParam5 = resolverParam5;
  }

  Completes<Response> execute(final Request request,
                              final T param1,
                              final R param2,
                              final U param3,
                              final I param4,
                              final J param5,
                              final Logger logger) {
    checkExecutor(executor);
    return executor.execute(request, param1, param2, param3, param4, param5, mediaTypeMapper, errorHandler, logger);
  }
  
  @FunctionalInterface
  public interface Handler5<T, R, U, I, J> {
    Completes<Response> execute(T param1, R param2, U param3, I param4, J param5);
  }

  @FunctionalInterface
  public interface ObjectHandler5<T, R, U, I, J> {
    Completes<ObjectResponse<?>> execute(T param1, R param2, U param3, I param4, J param5);
  }
  
  public RequestHandler5<T, R, U, I, J> handle(final Handler5<T, R, U, I, J> handler) {
    executor = RequestExecutor5.from(handler);
    return this;
  }

  public RequestHandler5<T, R, U, I, J> handle(final ObjectHandler5<T, R, U, I, J> handler) {
    executor = RequestObjectExecutor5.from(handler);
    return this;
  }

  public RequestHandler5<T, R, U, I, J> onError(final ErrorHandler errorHandler) {
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
    final J param5 = resolverParam5.apply(request, mappedParameters);
    return execute(request, param1, param2, param3, param4, param5, logger);
  }

  interface ParamExecutor5<T, R, U, I, J> {
    Completes<Response> execute(final Request request,
                                final T param1,
                                final R param2,
                                final U param3,
                                final I param4,
                                final J param5,
                                final MediaTypeMapper mediaTypeMapper,
                                final ErrorHandler errorHandler,
                                final Logger logger);
  }

  static class RequestExecutor5<T, R, U, I, J> extends RequestExecutor implements ParamExecutor5<T, R, U, I, J> {
    private final Handler5<T,R,U,I,J> handler;

    private RequestExecutor5(Handler5<T,R,U,I,J> handler) { this.handler = handler; }

    public Completes<Response> execute(final Request request,
                                       final T param1,
                                       final R param2,
                                       final U param3,
                                       final I param4,
                                       final J param5,
                                       final MediaTypeMapper mediaTypeMapper,
                                       final ErrorHandler errorHandler,
                                       final Logger logger) {
      return executeRequest(() -> handler.execute(param1, param2, param3, param4, param5), errorHandler, logger);
    }

    static <T,R,U,I,J> RequestExecutor5<T,R,U,I,J> from(final Handler5<T,R,U,I,J> handler) {
      return new RequestExecutor5<>(handler);}
  }

  static class RequestObjectExecutor5<T, R, U, I, J> extends RequestObjectExecutor implements ParamExecutor5<T, R, U, I, J> {
    private final ObjectHandler5<T,R,U,I,J> handler;
    private RequestObjectExecutor5(ObjectHandler5<T,R,U,I,J> handler) { this.handler = handler;}

    public Completes<Response> execute(final Request request,
                                       final T param1,
                                       final R param2,
                                       final U param3,
                                       final I param4,
                                       final J param5,
                                       final MediaTypeMapper mediaTypeMapper,
                                       final ErrorHandler errorHandler,
                                       final Logger logger) {
      return executeRequest(request,
                            mediaTypeMapper,
                            () -> handler.execute(param1, param2, param3, param4, param5),
                            errorHandler,
                            logger);
    }

    static <T,R,U,I,J> RequestObjectExecutor5<T,R,U,I,J> from(final ObjectHandler5<T,R,U,I,J> handler) {
      return new RequestObjectExecutor5<>(handler);}
  }
}
