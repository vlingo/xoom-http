package io.vlingo.http.resource;

import io.vlingo.common.Completes;
import io.vlingo.http.Response;

public class DefaultErrorHandler {

  private static ErrorHandler instance = (ex) ->  {
    if (ex instanceof MediaTypeNotSupportedException) {
      return Completes.withSuccess(Response.of(Response.Status.UnsupportedMediaType));
    } else if (ex instanceof IllegalArgumentException) {
      return Completes.withSuccess(Response.of(Response.Status.BadRequest));
    }
    else {
      return Completes.withSuccess(Response.of(Response.Status.InternalServerError));
    }
  };

  public static ErrorHandler instance() {
    return instance;
  }
}
