package io.vlingo.http.resource;

import io.vlingo.actors.Logger;
import io.vlingo.common.Completes;
import io.vlingo.common.Outcome;
import io.vlingo.http.Request;
import io.vlingo.http.Response;

import java.util.function.Supplier;

abstract class RequestObjectExecutor  {

  // todo: See missing code
  static Completes<Response> executeRequest(final Request request,
                                            final MediaTypeMapper mediaTypeMapper,
                                            final Supplier<Completes<ObjectResponse<?>>> executeAction,
                                            final ErrorHandler errorHandler,
                                            final Logger logger) {
    Completes<Response> responseCompletes;
    try {
      Outcome<Throwable, Completes<ObjectResponse<?>>> responseOutcome;
      // todo: Find way to use outcome to capture failure case that can be used in the .otherwise case
      Completes<ObjectResponse<?>> objectResponseCompletes = executeAction.get();
      responseCompletes = objectResponseCompletes
        .andThen(objResponse -> objResponse.fromRequest(request, mediaTypeMapper))
        .recoverFrom(exception -> {
          Completes<Response> errorResponse = ResourceErrorProcessor.resourceHandlerError(errorHandler, logger, exception);
          return errorResponse.await();
        });
    } catch (Exception exception) {
      responseCompletes = ResourceErrorProcessor.resourceHandlerError(errorHandler, logger, exception);
    }
    return responseCompletes;
  }
}
