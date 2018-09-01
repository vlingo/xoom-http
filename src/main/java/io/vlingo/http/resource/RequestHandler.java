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
import java.util.List;
import java.util.regex.Pattern;

public abstract class RequestHandler {
  final private Pattern p = Pattern.compile("\\{(.*?)\\}");

  public final Method method;
  public final String path;
  public final String actionSignature;

  RequestHandler(final Method method, final String path, final List<ParameterResolver<?>> parameterResolvers) {
    this.method = method;
    this.path = path;
    this.actionSignature = generateActionSignature(parameterResolvers);
  }

  abstract Response execute(final Request request, final Action.MappedParameters mappedParameters) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException;

  String generateActionSignature(final List<ParameterResolver<?>> parameterResolvers) {
    return "";
    /*
     if (path.replaceAll(" ", "").contains("{}")) {
      throw new IllegalArgumentException("Empty path parameter name for " + method + " " + path);
    }
    final Matcher m = p.matcher(path);
    final StringBuilder result = new StringBuilder();
    if (m.find()) {
      return "";
    }
    String paramName = m.group(1);
    return resolver.paramClass.getSimpleName() + " " + paramName;
     */
  }
}
