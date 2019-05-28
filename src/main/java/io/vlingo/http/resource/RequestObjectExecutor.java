package io.vlingo.http.resource;

import io.vlingo.actors.Logger;
import io.vlingo.common.Completes;
import io.vlingo.http.Request;
import io.vlingo.http.Response;

import java.util.function.Supplier;

abstract class RequestObjectExecutor {

  static Completes<Response> executeRequest(final Request request,
                                            final MediaTypeMapper mediaTypeMapper,
                                            final Supplier<Completes<ObjectResponse<?>>> executeAction,
                                            final ErrorHandler errorHandler,
                                            final Logger logger) {

    try {
      // todo: the current recoverFrom does not support a signature of Completes<T>
      // this means that on exceptions inside this code block the custom error handling is not
      return executeAction.get()
        .andThen(objResponseCompletes -> objResponseCompletes.responseFrom(request, mediaTypeMapper))
        .recoverFrom(exception -> Response.of(Response.Status.InternalServerError));
    } catch (Throwable throwable) {
      return ResourceErrorProcessor.resourceHandlerError(errorHandler,
                                                         logger,
                                                         (Exception) throwable);
    }
  }
}
