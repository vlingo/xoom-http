/*
 * Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.resource;

import io.vlingo.actors.Logger;
import io.vlingo.common.Completes;
import io.vlingo.common.Outcome;
import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.Response;

import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RequestHandler {
  public final Method method;
  public final String path;
  public final String actionSignature;
  final private Pattern pattern = Pattern.compile("\\{(.*?)\\}");

  RequestHandler(final Method method, final String path, final List<ParameterResolver<?>> parameterResolvers) {
    this.method = method;
    this.path = path;
    this.actionSignature = generateActionSignature(parameterResolvers);
  }

  private Completes<Response> defaultErrorResponse() {
    return Completes.withSuccess(Response.of(Response.Status.InternalServerError));
  }

  private Completes<Response> defaultErrorHandler(Exception ex) {
    if (ex instanceof MediaTypeNotSupported) {
      return Completes.withSuccess(Response.of(Response.Status.UnsupportedMediaType));
    } else if (ex instanceof IllegalArgumentException) {
      return Completes.withSuccess(Response.of(Response.Status.BadRequest));
    }
    else {
      return defaultErrorResponse();
    }
  }

  void checkHandlerOrThrowException(Object handler) {
    if (handler == null) {
      throw new HandlerMissingException("No handle defined for " + method.toString() + " " + path);
    }
  }

  abstract Completes<Response> execute(final Request request,
                                       final Action.MappedParameters mappedParameters,
                                       final Logger logger);


  Completes<Response> executeRequest(final Supplier<Completes<Response>> executeAction,
                                     final ErrorHandler errorHandler,
                                     final Logger logger) {
    Completes<Response> responseCompletes;
    try {
      responseCompletes = executeAction.get();
    } catch (Exception exception) {
      responseCompletes = resourceHandlerError(errorHandler, logger, exception);
    }
    return responseCompletes;
  }

  Completes<Response> executeObjectRequest(final Request request,
                                           final MediaTypeMapper mediaTypeMapper,
                                           final Supplier<Completes<ObjectResponse<?>>> executeAction,
                                           final ErrorHandler errorHandler,
                                           final Logger logger) {
    Completes<Response> responseCompletes;
    try {
      Outcome<Throwable, Completes<ObjectResponse<?>>> responseOutcome;
      // Find way to use outcome to capture failure case that can be used in the .otherwise case
      Completes<ObjectResponse<?>> objectResponseCompletes = executeAction.get();
      responseCompletes = objectResponseCompletes
        .andThen(objResponse -> objResponse.fromRequest(request, mediaTypeMapper))
        .recoverFrom(exception -> {
          Completes<Response> errorResponse = resourceHandlerError(errorHandler, logger, exception);
          // todo: Investigate Outcome
          return errorResponse.await();
        });
    } catch (Exception exception) {
      responseCompletes = resourceHandlerError(errorHandler, logger, exception);
    }
    return responseCompletes;
  }

  private Completes<Response> resourceHandlerError(ErrorHandler errorHandler, Logger logger, Exception exception) {
    Completes<Response> responseCompletes;
    if (errorHandler != null) {
      try {
        responseCompletes = errorHandler.handle(exception);
      } catch (Exception errorHandlerException) {
        logger.log("Exception thrown by error handler when handling error", exception);
        responseCompletes = defaultErrorResponse();
      }
    } else {
      logger.log("Exception thrown by Resource execution", exception);
      responseCompletes = defaultErrorHandler(exception);
    }
    return responseCompletes;
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
