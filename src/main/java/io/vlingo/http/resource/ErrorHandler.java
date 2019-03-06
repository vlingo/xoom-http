package io.vlingo.http.resource;

import io.vlingo.actors.Logger;
import io.vlingo.common.Completes;
import io.vlingo.http.Response;

public interface ErrorHandler {

  Completes<Response> handle(final Throwable error, final Logger logger);

  static ErrorHandler handleAllWith(final Response.Status status) {
    return (error, logger) -> Completes.withSuccess(Response.of(status));
  }
}
