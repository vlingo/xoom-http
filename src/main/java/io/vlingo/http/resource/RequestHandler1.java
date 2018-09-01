/*
 * Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.resource;

import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.Response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestHandler1<T> extends RequestHandler {
  final private Pattern p = Pattern.compile("\\{(.*?)\\}");
  final private String actionSignature;
  final ParameterResolver<T> resolver;
  private Handler1<T> handler;

  RequestHandler1(final Method method, final String path, final ParameterResolver<T> resolver) {
    super(method, path);
    this.actionSignature = generateActionSignature();
    this.resolver = resolver;
  }

  public <R> RequestHandler2<T, R> body(final Class<R> bodyClass) {
    return new RequestHandler2<>(method(), path(), resolver, ParameterResolver.body(bodyClass));
  }

  @FunctionalInterface
  public interface Handler1<T> {
    Response execute(T param1);
  }

  public RequestHandler1<T> handle(final Handler1<T> handler) {
    this.handler = handler;
    return this;
  }

  Response execute(final T param1) {
    if (handler == null)
      throw new HandlerMissingException("No handle defined for " + method().toString() + " " + path());
    return handler.execute(param1);
  }

  @SuppressWarnings("unchecked")
  @Override
  Response execute(Request request, Action.MappedParameters mappedParameters) {
    return execute(resolver.apply(request, mappedParameters));
  }

  @Override
  String actionSignature() {
    return actionSignature;
  }

  private String generateActionSignature() {
    if (path().replaceAll(" ", "").contains("{}")) {
      throw new IllegalArgumentException("Empty path parameter name for " + method() + " " + path());
    }
    final Matcher m = p.matcher(path());
    if (!m.find()) {
      return "";
    }
    String paramName = m.group(1);
    if (paramName.trim().isEmpty())
      throw new IllegalArgumentException("Empty path parameter for " + method() + " " + path());
    return resolver.paramClass.getSimpleName() + " " + paramName;
  }
}
