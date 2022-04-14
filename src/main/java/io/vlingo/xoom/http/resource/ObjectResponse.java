// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import java.util.function.Function;

import io.vlingo.xoom.http.Body;
import io.vlingo.xoom.http.Header;
import io.vlingo.xoom.http.Header.Headers;
import io.vlingo.xoom.http.Request;
import io.vlingo.xoom.http.RequestHeader;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.ResponseHeader;
import io.vlingo.xoom.http.Version;
import io.vlingo.xoom.http.media.ContentMediaType;
import io.vlingo.xoom.http.media.ResponseMediaTypeSelector;

public class ObjectResponse<T> {

  private static final ContentMediaType DEFAULT_MEDIA_TYPE = ContentMediaType.Json();

  private final Version version;
  private final Response.Status status;
  private final Headers<ResponseHeader> headers;
  private T entity;

  private ObjectResponse(final Version version,
                         final Response.Status status,
                         final Header.Headers<ResponseHeader> headers,
                         final T entity) {
    this.version = version;
    this.status = status;
    this.headers = headers;
    this.entity = entity;
  }

  public static <T> ObjectResponse<T> of(final Version version,
                                         final Response.Status status,
                                         final Header.Headers<ResponseHeader> headers,
                                         T entity) {
    return new ObjectResponse<>(version, status, headers, entity);
  }


  public static <T> ObjectResponse<T> of(final Response.Status status,
                                         final Header.Headers<ResponseHeader> headers,
                                         final T entity) {
    return new ObjectResponse<>(Version.Http1_1, status, headers, entity);
  }


  public static <T> ObjectResponse<T> of(final Response.Status status,
                                         final T entity) {
    return new ObjectResponse<>(Version.Http1_1, status, Header.Headers.empty(), entity);
  }


  @Override
  public String toString() {
    return into(new StringBuilder()::append).toString();
  }

  public Response responseFrom(Request request, MediaTypeMapper mapper) {
    final String acceptedMediaTypes = request.headerValueOr(RequestHeader.Accept, DEFAULT_MEDIA_TYPE.toString());
    final ResponseMediaTypeSelector responseMediaTypeSelector = new ResponseMediaTypeSelector(acceptedMediaTypes);
    final ContentMediaType responseContentMediaType = responseMediaTypeSelector.selectType(mapper.mappedMediaTypes());
    final String bodyContent = mapper.from(entity, responseContentMediaType);
    final Body body = Body.from(bodyContent);
    headers.add(ResponseHeader.of(ResponseHeader.ContentType, responseContentMediaType.toString()));
    return Response.of(version, status, headers, body);
  }

  private <R> R into(Function<String,R> appender) {
    // TODO: currently supports only HTTP/1.1
    appender.apply(Version.HTTP_1_1);
    appender.apply(" ");
    appender.apply(status.toString());
    appender.apply("\n");

    appendAllHeadersTo(appender);
    appender.apply("\n");
    return appender.apply(entity.toString());
  }

  private <R> void appendAllHeadersTo(Function<String, R> appender) {
    for (final ResponseHeader header : headers) {
      appender.apply(header.name);
      appender.apply(": ");
      appender.apply(header.value);
      appender.apply("\n");
    }
  }
}
