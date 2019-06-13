package io.vlingo.http.resource;

import io.vlingo.actors.Logger;
import io.vlingo.common.Completes;
import io.vlingo.common.Success;
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
      return executeAction.get()
        .andThen(objectResponse -> toResponse(objectResponse, request, mediaTypeMapper, errorHandler, logger));
    } catch(Exception ex) {
      return Completes.withFailure( ResourceErrorProcessor.resourceHandlerError(errorHandler, logger, ex));
    }
  }

  static Response toResponse(
                                          final ObjectResponse<?> objectResponse,
                                          final Request request,
                                          final MediaTypeMapper mediaTypeMapper,
                                          final ErrorHandler errorHandler,
                                          final Logger logger) {

      return Success.of(objectResponse.responseFrom(request, mediaTypeMapper))
        .resolve( ex -> ResourceErrorProcessor.resourceHandlerError(errorHandler, logger, (Exception) ex),
                  response -> response);

  }
}
