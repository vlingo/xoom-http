package io.vlingo.http.resource;

import io.vlingo.actors.Completes;
import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.Response;

import java.util.Arrays;

public class RequestHandler5<T, R, U, I, J> extends RequestHandler {
  final private ParameterResolver<T> resolverParam1;
  final private ParameterResolver<R> resolverParam2;
  final private ParameterResolver<U> resolverParam3;
  final private ParameterResolver<I> resolverParam4;
  final private ParameterResolver<J> resolverParam5;
  private Handler5<T, R, U, I, J> handler;

  RequestHandler5(final Method method,
                  final String path,
                  final ParameterResolver<T> resolverParam1,
                  final ParameterResolver<R> resolverParam2,
                  final ParameterResolver<U> resolverParam3,
                  final ParameterResolver<I> resolverParam4,
                  final ParameterResolver<J> resolverParam5) {
    super(method, path, Arrays.asList(resolverParam1, resolverParam2, resolverParam3, resolverParam4, resolverParam5));
    this.resolverParam1 = resolverParam1;
    this.resolverParam2 = resolverParam2;
    this.resolverParam3 = resolverParam3;
    this.resolverParam4 = resolverParam4;
    this.resolverParam5 = resolverParam5;
  }

  Completes<Response> execute(final T param1, final R param2, final U param3, final I param4, final J param5) {
    if (handler == null) throw new HandlerMissingException("No handle defined for " + method.toString() + " " + path);
    return handler.execute(param1, param2, param3, param4, param5);
  }

  public RequestHandler5<T, R, U, I, J> handle(final Handler5<T, R, U, I, J> handler) {
    this.handler = handler;
    return this;
  }

  @Override
  Completes<Response> execute(final Request request, final Action.MappedParameters mappedParameters) {
    final T param1 = resolverParam1.apply(request, mappedParameters);
    final R param2 = resolverParam2.apply(request, mappedParameters);
    final U param3 = resolverParam3.apply(request, mappedParameters);
    final I param4 = resolverParam4.apply(request, mappedParameters);
    final J param5 = resolverParam5.apply(request, mappedParameters);
    return execute(param1, param2, param3, param4, param5);
  }

  @FunctionalInterface
  public interface Handler5<T, R, U, I, J> {
    Completes<Response> execute(T param1, R param2, U param3, I param4, J param5);
  }
}
