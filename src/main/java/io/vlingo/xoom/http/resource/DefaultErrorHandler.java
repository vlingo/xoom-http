package io.vlingo.xoom.http.resource;

import io.vlingo.xoom.http.Response;

public class DefaultErrorHandler {

  private static ErrorHandler instance = (ex) ->  {
    if (ex instanceof MediaTypeNotSupportedException) {
      return Response.of(Response.Status.UnsupportedMediaType);
    } else if (ex instanceof IllegalArgumentException) {
      return Response.of(Response.Status.BadRequest);
    }
    else {
      return Response.of(Response.Status.InternalServerError);
    }
  };

  public static ErrorHandler instance() {
    return instance;
  }
}
