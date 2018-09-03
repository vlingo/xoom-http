/*
 * Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.resource;

import io.vlingo.actors.CompletesEventually;
import io.vlingo.http.Header;
import io.vlingo.http.Method;
import io.vlingo.http.Request;

import java.util.Collections;

public class RequestHandler0 extends RequestHandler {
  private Handler0 handler;

  RequestHandler0(final Method method, final String path) {
    super(method, path, Collections.emptyList());
  }

  @FunctionalInterface
  public interface Handler0 {
    void execute(CompletesEventually completes);
  }

  public RequestHandler0 handle(final Handler0 handler) {
    this.handler = handler;
    return this;
  }

  void execute(final CompletesEventually completes) {
    if (handler == null) throw new HandlerMissingException("No handle defined for " + method.toString() + " " + path);
    handler.execute(completes);
  }

  @Override
  void execute(final Request request,
                   final Action.MappedParameters mappedParameters,
                   final CompletesEventually completes) {
    execute(completes);
  }

  // region FluentAPI
  public <T> RequestHandler1<T> param(final Class<T> paramClass) {
    return new RequestHandler1<>(method, path, ParameterResolver.path(0, paramClass));
  }

  public <T> RequestHandler1<T> body(final Class<T> paramClass) {
    return new RequestHandler1<>(method, path, ParameterResolver.body(paramClass));
  }

  public RequestHandler1<String> query(final String name) {
    return query(name, String.class);
  }

  public <T> RequestHandler1<T> query(final String name, final Class<T> queryClass) {
    return new RequestHandler1<>(method, path, ParameterResolver.query(name, queryClass));
  }

  public RequestHandler1<Header> header(final String name) {
    return new RequestHandler1<>(method, path, ParameterResolver.header(name));
  }
  // endregion
}
