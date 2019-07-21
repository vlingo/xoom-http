// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.util.function.Supplier;

import io.vlingo.actors.Logger;
import io.vlingo.common.Completes;
import io.vlingo.http.Response;

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
