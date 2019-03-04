/*
 * Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.resource;

import io.vlingo.common.Completes;
import io.vlingo.common.Outcome;
import io.vlingo.http.Header;
import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.Version;
import io.vlingo.http.Body;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseError;
import io.vlingo.http.ResponseHeader;

import java.util.Collections;
import java.util.Objects;

import static io.vlingo.http.resource.RequestHandler0SubTypes.*;

public class RequestHandler0SubTypes extends RequestHandler<MyResponse> {
  private Handler0 handler;
  private ErrorHandler errorHandler;

  static class MyResponse extends Response {
    final String extraData;
    protected MyResponse(Version version, Status status, Header.Headers<ResponseHeader> headers, Body entity, String extraData) {
      super(version, status, headers, entity);
      this.extraData = extraData;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      MyResponse that = (MyResponse) o;
      return extraData.equals(that.extraData);
    }

    @Override
    public int hashCode() {
      return Objects.hash(extraData);
    }
  }

  RequestHandler0SubTypes(final Method method, final String path) {
    super(method, path, Collections.emptyList());
  }

  Completes<Response> defaultErrorResponse() {
    return Completes.withSuccess(Response.of(Response.Status.InternalServerError));
  }

  public RequestHandler0SubTypes handle(final Handler0 handler) {
    this.handler = handler;
    return this;
  }

  public RequestHandler0SubTypes onError(final ErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
    return this;
  }

  Completes<Outcome<ResponseError, MyResponse>> execute() {
    return executeRequest(() -> handler.execute(), errorHandler);
  }

  @Override
  Completes<Outcome<ResponseError, MyResponse>> execute(final Request request,
                                                      final Action.MappedParameters mappedParameters) {
    checkHandlerOrThrowException(handler);
    return executeRequest( () -> handler.execute(), errorHandler);
  }

  @FunctionalInterface
  public interface Handler0 {
    Completes<MyResponse> execute();
  }

  // region FluentAPI
  public <T> RequestHandler1<T> param(final Class<T> paramClass) {
    return new RequestHandler1<>(method, path, ParameterResolver.path(0, paramClass), errorHandler);
  }

  public <T> RequestHandler1<T> body(final Class<T> paramClass) {
    return new RequestHandler1<>(method, path, ParameterResolver.body(paramClass), errorHandler);
  }

  public <T> RequestHandler1<T> body(final Class<T> paramClass, final Class<? extends Mapper> mapperClass ) {
    return body(paramClass, mapperFrom(mapperClass));
  }

  public <T> RequestHandler1<T> body(final Class<T> paramClass, final Mapper mapper ) {
    return new RequestHandler1<>(method, path, ParameterResolver.body(paramClass, mapper), errorHandler);
  }

  public RequestHandler1<String> query(final String name) {
    return query(name, String.class);
  }

  public <T> RequestHandler1<T> query(final String name, final Class<T> type) {
    return query(name, type, null);
  }

  public <T> RequestHandler1<T> query(final String name, final Class<T> type, final T defaultValue) {
    return new RequestHandler1<>(method, path, ParameterResolver.query(name, type, defaultValue), errorHandler);
  }

  public RequestHandler1<Header> header(final String name) {
    return new RequestHandler1<>(method, path, ParameterResolver.header(name), errorHandler);
  }
  // endregion
}
