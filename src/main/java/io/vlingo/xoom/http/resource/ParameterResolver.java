// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import io.vlingo.xoom.http.*;
import io.vlingo.xoom.http.media.ContentMediaType;

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
      throw new IllegalArgumentException("Value " + value + " is of mimeType " + mappedParameters.mapped.get(position).type + " instead of " + paramClass.getSimpleName());
    });
  }

  public static <T> ParameterResolver<T> body(final Class<T> bodyClass) {
      return body(bodyClass, DefaultMediaTypeMapper.instance());
  }

  public static <T> ParameterResolver<T> body(final Class<T> bodyClass, final Mapper mapper) {
    return new ParameterResolver<>(Type.BODY, bodyClass, ((request, mappedParameters) ->
      mapper.from(request.body.toString(), bodyClass)));
  }

  @SuppressWarnings("unchecked")
  public static <T> ParameterResolver<T> body(final Class<T> bodyClass, final MediaTypeMapper mediaTypeMapper) {
    if (bodyClass.isAssignableFrom(RequestData.class)) {
      return (ParameterResolver<T>) new ParameterResolver<RequestData>(Type.BODY, RequestData.class, ((request, mappedParameters) -> {
        // This is a fall-back when content-type not provided for backwards compat for curl/cmd line users
        final String bodyMediaType = bodyMediaTypeOrFallback(request);
        final RequestHeader contentEncodingHeader = request.headers.headerOfOrDefault(ResponseHeader.ContentEncoding, RequestHeader.contentEncoding());
        final ContentEncoding contentEncoding = ContentEncoding.parseFromHeader(contentEncodingHeader.value);
        return new RequestData(request.body, ContentMediaType.parseFromDescriptor(bodyMediaType), contentEncoding);
      }));
    }
    return new ParameterResolver<T>(Type.BODY, bodyClass, ((request, mappedParameters) -> {
      // This is a fall-back when content-type not provided for backwards compat for curl/cmd line users
      final String bodyMediaType = bodyMediaTypeOrFallback(request);
      return mediaTypeMapper.from(request.body.toString(), ContentMediaType.parseFromDescriptor(bodyMediaType), bodyClass);
    }));
  }

  private static String bodyMediaTypeOrFallback(final Request request) {
    String assumedBodyContentType = ContentMediaType.Json().toString();
    return request.headerValueOr(RequestHeader.ContentType, assumedBodyContentType);
  }

  public static ParameterResolver<Header> header(final String headerName) {
    return new ParameterResolver<>(Type.HEADER, Header.class, ((request, mappedParameters) ->
      request.headerOf(headerName)));
  }

  public static ParameterResolver<String> query(final String name) {
    return query(name, String.class, null);
  }

  public static <T> ParameterResolver<T> query(final String name, final Class<T> type) {
    return query(name, type, null);
  }

  public static <T> ParameterResolver<T> query(final String name, final Class<T> type, final T defaultValue) {
    return new ParameterResolver<>(Type.QUERY, type, (((request, mappedParameters) -> {
      String value;
      try {
        value = request.queryParameters().valuesOf(name).get(0);
      } catch (IllegalArgumentException | NullPointerException e) {
        return  defaultValue;
      }
      if (type == Integer.class) {
        return type.cast(Integer.valueOf(value));
      } else if (type == String.class) {
        return type.cast(value);
      } else if (type == Float.class) {
        return type.cast(Float.valueOf(value));
      } else if (type == Long.class) {
        return type.cast(Integer.valueOf(value));
      } else if (type == Boolean.class) {
        return type.cast(Boolean.valueOf(value));
      } else if (type == Short.class) {
        return type.cast(Short.valueOf(value));
      } else if (type == Byte.class) {
        return type.cast(Byte.valueOf(value));
      }
      throw new IllegalArgumentException("unknown mimeType " + type.getSimpleName());
    })));
  }

  public T apply(final Request request, final Action.MappedParameters mappedParameters) {
    return resolver.apply(request, mappedParameters);
  }

  enum Type {
    PATH,
    BODY,
    HEADER,
    QUERY
  }
}
