// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import java.util.function.Supplier;

import io.vlingo.xoom.actors.Logger;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.common.Success;
import io.vlingo.xoom.http.Request;
import io.vlingo.xoom.http.Response;

abstract class RequestObjectExecutor {

  static <O> Completes<Response> executeRequest(final Request request,
                                            final MediaTypeMapper mediaTypeMapper,
                                            final Supplier<Completes<ObjectResponse<O>>> executeAction,
                                            final ErrorHandler errorHandler,
                                            final Logger logger) {

    try {
      return executeAction.get()
        .andFinally(objectResponse -> toResponse(objectResponse, request, mediaTypeMapper, errorHandler, logger));
    } catch(Exception ex) {
      return Completes.withFailure( ResourceErrorProcessor.resourceHandlerError(errorHandler, logger, ex));
    }
  }

  static <O> Response toResponse(
                                          final ObjectResponse<O> objectResponse,
                                          final Request request,
                                          final MediaTypeMapper mediaTypeMapper,
                                          final ErrorHandler errorHandler,
                                          final Logger logger) {

      return Success.of(objectResponse.responseFrom(request, mediaTypeMapper))
        .resolve( ex -> ResourceErrorProcessor.resourceHandlerError(errorHandler, logger, (Exception) ex),
                  response -> response);

  }
}
