/*
 * Copyright © 2012-2021 VLINGO LABS. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.xoom.http.resource;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.vlingo.xoom.actors.Logger;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.http.Method;
import io.vlingo.xoom.http.Request;
import io.vlingo.xoom.http.Response;

public abstract class RequestHandler {
  public final Method method;
  public final String path;
  public final String actionSignature;
  public final String contentSignature;
  public final Class<?> bodyType;
  private final Pattern pattern = Pattern.compile("\\{(.*?)\\}");
  protected MediaTypeMapper mediaTypeMapper;
  protected ErrorHandler errorHandler;

  protected RequestHandler(final Method method, final String path, final List<ParameterResolver<?>> parameterResolvers) {
    this.method = method;
    this.path = path;
    this.actionSignature = generateActionSignature(parameterResolvers);
    this.contentSignature = detectRequestBodyType(parameterResolvers).map(Class::getSimpleName).orElse(null);
    this.bodyType = detectRequestBodyType(parameterResolvers).orElse(null);
    this.errorHandler = DefaultErrorHandler.instance();
    this.mediaTypeMapper = DefaultMediaTypeMapper.instance();
  }

  protected RequestHandler(final Method method,
                           final String path,
                           final List<ParameterResolver<?>> parameterResolvers,
                           final ErrorHandler errorHandler,
                           final MediaTypeMapper mediaTypeMapper) {
    this.method = method;
    this.path = path;
    this.actionSignature = generateActionSignature(parameterResolvers);
    this.contentSignature = detectRequestBodyType(parameterResolvers).map(Class::getSimpleName).orElse(null);
    this.bodyType = detectRequestBodyType(parameterResolvers).orElse(null);
    this.errorHandler = errorHandler;
    this.mediaTypeMapper = mediaTypeMapper;
  }

  protected Completes<Response> runParamExecutor(Object paramExecutor, Supplier<Completes<Response>> executeRequest) {
    if (paramExecutor == null) {
      throw new HandlerMissingException("No handler defined for " + method.toString() + " " + path);
    }
    return executeRequest.get();
  }

  protected abstract Completes<Response> execute(final Request request,
                                                 final Action.MappedParameters mappedParameters,
                                                 final Logger logger);

  private Optional<? extends Class<?>> detectRequestBodyType(final List<ParameterResolver<?>> parameterResolvers) {
    return parameterResolvers.stream().filter(parameterResolver -> parameterResolver.type == ParameterResolver.Type.BODY)
      .map(p -> p.paramClass)
      .findFirst();
  }

  private String generateActionSignature(final List<ParameterResolver<?>> parameterResolvers) {
    checkOrder(parameterResolvers);

    if (path.replaceAll(" ", "").contains("{}")) {
      throw new IllegalArgumentException("Empty path parameter name for " + method + " " + path);
    }

    final StringBuilder result = new StringBuilder();
    final Matcher matcher = pattern.matcher(path);
    boolean first = true;
    for (ParameterResolver<?> resolver : parameterResolvers) {
      if (resolver.type == ParameterResolver.Type.PATH) {
        matcher.find();
        if (first) {
          first = false;
        } else {
          result.append(", ");
        }
        result.append(resolver.paramClass.getSimpleName()).append(" ").append(matcher.group(1));
      }
    }
    return result.toString();
  }

  private void checkOrder(final List<ParameterResolver<?>> parameterResolvers) {
    boolean firstNonPathResolver = false;
    for (ParameterResolver<?> resolver : parameterResolvers) {
      if (resolver.type != ParameterResolver.Type.PATH) {
        firstNonPathResolver = true;
      }
      if (firstNonPathResolver && resolver.type == ParameterResolver.Type.PATH) {
        throw new IllegalArgumentException("Path parameters are unsorted");
      }
    }
  }

  protected Mapper mapperFrom(final Class<? extends Mapper> mapperClass) {
    try {
      return mapperClass.newInstance();
    } catch (Exception e) {
      throw new IllegalArgumentException("Cannot instantiate mapper class: " + mapperClass.getName());
    }
  }
}
