package io.vlingo.xoom.http.resource;

import io.vlingo.xoom.http.Response;

public interface ErrorHandler {

  Response handle(final Throwable error);

  static ErrorHandler handleAllWith(final Response.Status status) {
    return (error) -> Response.of(status);
  }
}
