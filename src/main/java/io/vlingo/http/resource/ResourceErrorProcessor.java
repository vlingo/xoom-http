package io.vlingo.http.resource;

import io.vlingo.actors.Logger;
import io.vlingo.common.Completes;
import io.vlingo.http.Response;

public class ResourceErrorProcessor {

  static Completes<Response> defaultErrorResponse() {
    return Completes.withSuccess(Response.of(Response.Status.InternalServerError));
  }

  static Completes<ObjectResponse<?>> resourceHandlerObjectError(ErrorHandler errorHandler, Logger logger, Exception exception) {
    return Completes.withSuccess( ObjectResponse.of(Response.Status.Ok, "", String.class));
  }

  static Completes<Response> resourceHandlerError(ErrorHandler errorHandler, Logger logger, Exception exception) {
    Completes<Response> responseCompletes;
    try {
      logger.log("Exception thrown by Resource execution", exception);
      responseCompletes = (errorHandler != null) ?
        errorHandler.handle(exception) :
        DefaultErrorHandler.instance().handle(exception);
    } catch (Exception errorHandlerException) {
      logger.log("Exception thrown by error handler when handling error", exception);
      responseCompletes = defaultErrorResponse();
    }
    return responseCompletes;
  }
}
