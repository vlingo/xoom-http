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

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestHandler1<T> extends RequestHandler {
  final Class<T> param1Class;
  private Handler1<T> handler;
  final private Pattern p = Pattern.compile("\\{(.*?)\\}");
  final private String actionSignature;

  RequestHandler1(final Method method, final String path, final Class<T> param1) {
    super(method, path);
    this.param1Class = param1;
    this.actionSignature = generateActionSignature();
  }

  public <R> RequestHandler2<T,R> body(final Class<R> bodyClass) {
    return new RequestHandler2<>(method(), path(), param1Class, bodyClass);
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
    if(handler == null) throw new HandlerMissingException("No handle defined for " + method().toString() + " " + path());
    return handler.execute(param1);
  }

  @SuppressWarnings("unchecked")
  @Override
  Response execute(Request request, Action.MappedParameters mappedParameters) {
    final T param1 = (T) mappedParameters.mapped.get(0).value;
    return execute(param1);
  }

  @Override
  String actionSignature() {
    return actionSignature;
  }

  private String generateActionSignature() {
    final Matcher m = p.matcher(path());
    m.find();
    String paramName = m.group(1);
    if(paramName.trim().isEmpty()) throw new IllegalArgumentException("Empty path parameter for " + method() + " " + path());
    return param1Class.getSimpleName() + " " + paramName;
  }
}
