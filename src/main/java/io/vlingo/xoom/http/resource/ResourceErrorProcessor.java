package io.vlingo.xoom.http.resource;

import io.vlingo.xoom.actors.Logger;
import io.vlingo.xoom.http.Response;

public class ResourceErrorProcessor {

  static Response defaultErrorResponse() {
    return Response.of(Response.Status.InternalServerError);
  }

  static Response resourceHandlerError(ErrorHandler errorHandler, Logger logger, Throwable exception) {
    Response response;
    try {
      logger.error("Exception thrown by Resource execution", exception);
      response = (errorHandler != null) ?
        errorHandler.handle(exception) :
        DefaultErrorHandler.instance().handle(exception);
    } catch (Exception errorHandlerException) {
      logger.error("Exception thrown by error handler when handling error", exception);
      response = defaultErrorResponse();
    }
    return response;
  }
}
