package io.vlingo.http.resource;

import io.vlingo.actors.Logger;
import io.vlingo.common.Completes;
import io.vlingo.http.Response;

import java.util.function.Supplier;

abstract class RequestExecutor {

  protected Completes<Response> executeRequest(final Supplier<Completes<Response>> executeAction,
                                               final ErrorHandler errorHandler,
                                               final Logger logger) {
    Completes<Response> responseCompletes;
    try {
      responseCompletes = executeAction.get();
    } catch (Exception exception) {
      responseCompletes = ResourceErrorProcessor.resourceHandlerError(errorHandler, logger, exception);
    }
    return responseCompletes;
  }

}
