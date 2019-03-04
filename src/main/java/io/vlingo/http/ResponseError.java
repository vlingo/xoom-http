package io.vlingo.http;

import io.vlingo.common.Failure;
import io.vlingo.common.Outcome;

public class ResponseError extends RuntimeException {
  private Response response;

  public ResponseError(Response response) {
    this.response = response;
  }

  public static <T> Outcome<ResponseError, T> asFailure(Response response) {
    return Failure.of(new ResponseError(response));
  }

  public static Response unwrap(Outcome<? extends ResponseError, ?> outcome) {
    if (!(outcome instanceof Failure)) {
      throw new IllegalArgumentException("trying to unwrap a non failure");
    }
    @SuppressWarnings("unchecked")
    Failure<? extends ResponseError, ?> failure = (Failure<? extends ResponseError, ?>) outcome;
    try {
      failure.get();
    } catch (ResponseError e) {
      return e.response;
    }
    throw new IllegalStateException("won't reach here");
  }
}
