package io.vlingo.http.resource;

import io.vlingo.actors.Logger;
import io.vlingo.http.Response;

public class ResourceErrorProcessor {

  static Response defaultErrorResponse() {
    return Response.of(Response.Status.InternalServerError);
  }

  static Response resourceHandlerError(ErrorHandler errorHandler, Logger logger, Exception exception) {
    Response response;
    try {
      logger.log("Exception thrown by Resource execution", exception);
      response = (errorHandler != null) ?
        errorHandler.handle(exception) :
        DefaultErrorHandler.instance().handle(exception);
    } catch (Exception errorHandlerException) {
      logger.log("Exception thrown by error handler when handling error", exception);
      response = defaultErrorResponse();
    }
    return response;
  }
}
