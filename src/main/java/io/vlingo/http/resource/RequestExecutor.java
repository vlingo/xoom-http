package io.vlingo.http.resource;

import io.vlingo.actors.Logger;
import io.vlingo.common.Completes;
import io.vlingo.http.Response;

import java.util.function.Supplier;

abstract class RequestExecutor {

  static Completes<Response> executeRequest(final Supplier<Completes<Response>> executeAction,
                                            final ErrorHandler errorHandler,
                                            final Logger logger) {

    try {
      return executeAction.get()
        .recoverFrom(ex -> ResourceErrorProcessor.resourceHandlerError(errorHandler, logger, ex));
    } catch(Exception ex) {
      return Completes.withFailure(ResourceErrorProcessor.resourceHandlerError(errorHandler, logger, ex));
    }
  }

}
