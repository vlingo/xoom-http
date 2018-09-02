package io.vlingo.http.resource;

import io.vlingo.http.Header;
import io.vlingo.http.Request;

import java.util.function.BiFunction;

class ParameterResolver<T> {
  public final Type type;
  public final Class<T> paramClass;
  private final BiFunction<Request, Action.MappedParameters, T> resolver;

  private ParameterResolver(final Type type, final Class<T> paramClass, final BiFunction<Request, Action.MappedParameters, T> resolver) {
    this.type = type;
    this.paramClass = paramClass;
    this.resolver = resolver;
  }

  @SuppressWarnings("unchecked")
  public static <T> ParameterResolver<T> path(final int position, final Class<T> paramClass) {
    return new ParameterResolver<>(Type.PATH, paramClass, (request, mappedParameters) -> {
      Object value = mappedParameters.mapped.get(position).value;
      if (paramClass.isInstance(value)) {
        return (T) value;
      }
      throw new IllegalArgumentException("Value " + value + " is of type " + mappedParameters.mapped.get(position).type + " instead of " + paramClass.getSimpleName());
    });
  }

  public static <T> ParameterResolver<T> body(final Class<T> bodyClass) {
      return body(bodyClass, DefaultMapper.instance);
  }

  public static <T> ParameterResolver<T> body(final Class<T> bodyClass, final Mapper mapper) {
    return new ParameterResolver<>(Type.BODY, bodyClass, ((request, mappedParameters) ->
      mapper.from(request.body.toString(), bodyClass)));
  }

  public static ParameterResolver<Header> header(String headerName) {
    return new ParameterResolver<>(Type.HEADER, Header.class, ((request, mappedParameters) ->
      request.headerOf(headerName)));
  }

  public static ParameterResolver<String> query(String name) {
    return new ParameterResolver<>(Type.QUERY, String.class, (((request, mappedParameters) ->
      request.queryParameters().valuesOf(name).get(0))));
  }

  public static <T> ParameterResolver<T> query(String name, Class<T> type) {
    return new ParameterResolver<>(Type.QUERY, type, (((request, mappedParameters) -> {
      if (type == Integer.class) {
        return type.cast(Integer.valueOf(request.queryParameters().valuesOf(name).get(0)));
      } else if (type == String.class) {
        return type.cast(request.queryParameters().valuesOf(name).get(0));
      } else if (type == Float.class) {
        return type.cast(Float.valueOf(request.queryParameters().valuesOf(name).get(0)));
      } else if (type == Long.class) {
        return type.cast(Integer.valueOf(request.queryParameters().valuesOf(name).get(0)));
      } else if (type == Boolean.class) {
        return type.cast(Boolean.valueOf(request.queryParameters().valuesOf(name).get(0)));
      } else if (type == Short.class) {
        return type.cast(Short.valueOf(request.queryParameters().valuesOf(name).get(0)));
      } else if (type == Byte.class) {
        return type.cast(Byte.valueOf(request.queryParameters().valuesOf(name).get(0)));
      }
      throw new IllegalArgumentException("unknown type " + type.getSimpleName());
    })));
  }

  public T apply(Request request, Action.MappedParameters mappedParameters) {
    return resolver.apply(request, mappedParameters);
  }

  enum Type {
    PATH,
    BODY,
    HEADER,
    QUERY
  }
}
