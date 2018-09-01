package io.vlingo.http.resource;

import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.Response;

import java.util.Arrays;

public class RequestHandler3<T,R,U> extends RequestHandler {
  final private ParameterResolver<T> resolverParam1;
  final private ParameterResolver<R> resolverParam2;
  final private ParameterResolver<U> resolverParam3;
  private Handler3<T,R,U> handler;

  RequestHandler3(final Method method,
                  final String path,
                  final ParameterResolver<T> resolverParam1,
                  final ParameterResolver<R> resolverParam2,
                  final ParameterResolver<U> resolverParam3) {
    super(method, path, Arrays.asList(resolverParam1, resolverParam2, resolverParam3));
    this.resolverParam1 = resolverParam1;
    this.resolverParam2 = resolverParam2;
    this.resolverParam3 = resolverParam3;
  }

  @FunctionalInterface
  public interface Handler3<T,R,U> {
    Response execute(T param1, R param2, U param3);
  }

  Response execute(final T param1, final R param2, final U param3) {
    if(handler == null) throw new HandlerMissingException("No handle defined for " + method.toString() + " " + path);
    return handler.execute(param1, param2, param3);
  }

  @Override
  Response execute(Request request, Action.MappedParameters mappedParameters) {
    final T param1 = resolverParam1.apply(request, mappedParameters);
    final R param2 = resolverParam2.apply(request, mappedParameters);
    final U param3 = resolverParam3.apply(request, mappedParameters);
    return this.execute(param1, param2, param3);
  }

  public RequestHandler3<T, R, U> handle(final Handler3<T, R, U> handler) {
    this.handler = handler;
    return this;
  }
}
