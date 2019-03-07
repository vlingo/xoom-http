package io.vlingo.http.resource;

import io.vlingo.actors.Logger;
import io.vlingo.common.Completes;
import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.Response;
import io.vlingo.http.sample.user.NameData;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static io.vlingo.http.Response.Status.InternalServerError;
import static org.junit.Assert.assertEquals;

public class RequestHandlerTest extends RequestHandlerTestBase {

  @Test
  public void executionErrorUsesErrorHandlerWhenExceptionThrown() {
    Response.Status testStatus = Response.Status.BadRequest;

    final RequestHandlerFake handler = new RequestHandlerFake(Method.GET,
      "/hello",
      new ArrayList<>(),
      () -> { throw new RuntimeException("Handler failed"); }
    );

    ErrorHandler validHandler = (exception) -> {
      Assert.assertTrue( exception instanceof RuntimeException);
      return Completes.withSuccess(Response.of(testStatus));
    };

    Response response = handler.execute(validHandler, logger).await();
    assertResponsesAreEquals(Response.of(testStatus), response);
  }

  @Test
  public void internalErrorReturnedWhenErrorHandlerThrowsException() {
    final RequestHandlerFake handler = new RequestHandlerFake(Method.GET,
      "/hello",
      new ArrayList<>(),
      () -> { throw new RuntimeException("Handler failed"); }
    );

    ErrorHandler badHandler = (exception) -> {
      throw new IllegalArgumentException("foo");
    };

    Response response = handler.execute(badHandler, logger).await();
    assertResponsesAreEquals(Response.of(InternalServerError), response);
  }

  @Test
  public void internalErrorReturnedWhenNoErrorHandlerDefined() {
    final RequestHandlerFake handler = new RequestHandlerFake(Method.GET,
      "/hello",
      new ArrayList<>(),
      () -> { throw new RuntimeException("Handler failed"); }
    );

    Response response = handler.execute(null, logger).await();
    assertResponsesAreEquals(Response.of(InternalServerError), response);
  }



  @Test
  public void generateActionSignatureWhenNoPathIsSpecifiedIsEmptyString() {
    final RequestHandlerFake handler = new RequestHandlerFake(
      Method.GET,
      "/hello",
      Collections.singletonList(ParameterResolver.body(NameData.class)));

    assertEquals("", handler.actionSignature);
  }

  @Test
  public void generateActionSignatureWithOnePathParameterReturnsSignatureWithOneParam() {
    final RequestHandlerFake handler = new RequestHandlerFake(
      Method.GET,
      "/user/{userId}",
      Collections.singletonList(ParameterResolver.path(0, String.class)));

    assertEquals("String userId", handler.actionSignature);
  }

  @Test
  public void generateActionWithTwoPathParameters() {
    final RequestHandlerFake handler = new RequestHandlerFake(
      Method.GET,
      "/user/{userId}/comment/{commentId}",
      Arrays.asList(ParameterResolver.path(0, String.class), ParameterResolver.path(0, Integer.class)));

    assertEquals("String userId, Integer commentId", handler.actionSignature);
  }

  @Test
  public void generateActionWithOnePathParameterAndBodyJustReturnPathParameterSignature() {
    final RequestHandlerFake handler = new RequestHandlerFake(
      Method.GET,
      "/user/{userId}",
      Arrays.asList(ParameterResolver.path(0, String.class), ParameterResolver.body(NameData.class)));

    assertEquals("String userId", handler.actionSignature);
  }

  @Test(expected = IllegalArgumentException.class)
  public void unsortedPathParametersThrowsException() {
    new RequestHandlerFake(
      Method.GET,
      "/user/{userId}",
      Arrays.asList(ParameterResolver.body(NameData.class), ParameterResolver.path(0, String.class)));
  }
}

class RequestHandlerFake extends RequestHandler {

  Supplier<Completes<Response>> handler;

  RequestHandlerFake(Method method, String path, List<ParameterResolver<?>> parameterResolvers) {
    super(method, path, parameterResolvers);
    handler = () -> Completes.withSuccess(Response.of(Response.Status.Ok));
  }

  RequestHandlerFake(Method method, String path,
                     List<ParameterResolver<?>> parameterResolvers,
                     Supplier<Completes<Response>> handler) {
    super(method, path, parameterResolvers);
    this.handler = handler;
  }

  @Override
  Completes<Response> execute(final Request request,
                              final Action.MappedParameters mappedParameters,
                              final Logger logger) {
    throw new UnsupportedOperationException();
  }

  Completes<Response> execute(ErrorHandler errorHandler, Logger logger) {
    return executeRequest(handler, errorHandler, logger);
  }

}
