// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import java.util.function.Supplier;

import io.vlingo.xoom.actors.Logger;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.http.Response;

abstract class RequestExecutor {

  static Completes<Response> executeRequest(final Supplier<Completes<Response>> executeAction,
                                            final ErrorHandler errorHandler,
                                            final Logger logger) {

    try {
      final Completes<Response> resourceResponse =
              executeAction
                .get()
                .otherwise((Response failed) -> failed)
                .recoverFrom(ex -> ResourceErrorProcessor.resourceHandlerError(errorHandler, logger, ex));
      return resourceResponse;
    } catch(Exception ex) {
      return Completes.withFailure(ResourceErrorProcessor.resourceHandlerError(errorHandler, logger, ex));
    }
  }

}
