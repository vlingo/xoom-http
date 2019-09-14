package io.vlingo.http.resource;

import io.vlingo.actors.Logger;
import io.vlingo.common.Completes;
import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseHeader;
import io.vlingo.http.media.ContentMediaType;
import io.vlingo.http.resource.serialization.JsonSerialization;
import io.vlingo.http.sample.user.NameData;
import io.vlingo.http.sample.user.model.Name;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.vlingo.http.Response.Status.*;
import static io.vlingo.http.resource.RequestHandler0.*;
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

    ErrorHandler customHandler = (exception) -> {
      Assert.assertTrue( exception instanceof RuntimeException);
      return Response.of(testStatus);
    };

    Response response = handler.execute(Request.method(Method.GET), customHandler, logger).await();
    assertResponsesAreEquals(Response.of(testStatus), response);
  }

  @Test
  public void executionErrorObjectUsesErrorHandlerWhenExceptionThrown() {
    final Response.Status testStatus = Response.Status.BadRequest;
    final ErrorHandler validHandler = (exception) -> {
      Assert.assertTrue( exception instanceof RuntimeException);
      return Response.of(testStatus);
    };

    final RequestObjectHandlerFake handler = new RequestObjectHandlerFake(Method.GET,
      "/hello",
      validHandler,
      () -> { throw new RuntimeException("Handler failed"); }
    );

    Response response = handler.execute(Request.method(Method.GET), null, logger).await();
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

    Response response = handler.execute(Request.method(Method.GET), badHandler, logger).await();
    assertResponsesAreEquals(Response.of(InternalServerError), response);
  }

  @Test
  public void internalErrorReturnedWhenNoErrorHandlerDefined() {
    final RequestHandlerFake handler = new RequestHandlerFake(Method.GET,
      "/hello",
      new ArrayList<>(),
      () -> { throw new RuntimeException("Handler failed"); }
    );

    Response response = handler.execute(Request.method(Method.GET), (ErrorHandler) null, logger).await();
    assertResponsesAreEquals(Response.of(InternalServerError), response);
  }

  @Test
  public void mappingNotAvailableReturnsMediaTypeNotFoundResponse() {
    final RequestHandlerFake handler = new RequestHandlerFake(Method.GET,
      "/hello",
      new ArrayList<>(),
      () -> { throw new MediaTypeNotSupportedException("foo/bar"); }
    );

    Response response = handler.execute(Request.method(Method.GET), (ErrorHandler)null, logger).await();
    assertResponsesAreEquals(Response.of(UnsupportedMediaType), response);
  }

  @Test
  public void objectResponseMappedToContentType() {
    final Name name = new Name("first", "last");
    final RequestObjectHandlerFake handler = new RequestObjectHandlerFake(Method.GET,
      "/hello",
      () -> Completes.withSuccess(ObjectResponse.of(Ok, name))
    );

    Response response = handler.execute(Request.method(Method.GET), null, logger).await();
    String nameAsJson = JsonSerialization.serialized(name);
    assertResponsesAreEquals(
      Response.of(Ok,
                  ResponseHeader.headers(ResponseHeader.ContentType, ContentMediaType.Json().toString()),
                  nameAsJson),
      response);
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

class RequestObjectHandlerFake extends RequestHandler {

  private ParamExecutor0 executor;
  private ErrorHandler errorHandler;

  RequestObjectHandlerFake(Method method, String path, ObjectHandler0 handler) {
    super(method, path, new ArrayList<>());
    this.executor = (request, mediaTypeMapper1, errorHandler1, logger) ->
      RequestObjectExecutor.executeRequest(request, mediaTypeMapper1, () -> handler.execute(), errorHandler, logger);
    this.errorHandler = null;
  }

  RequestObjectHandlerFake(Method method, String path, ErrorHandler errorHandler, ObjectHandler0 handler) {
    super(method, path, new ArrayList<>());
    executor = ((request, mediaTypeMapper1, errorHandler1, logger1) ->
      RequestObjectExecutor.executeRequest(request, mediaTypeMapper1, () -> handler.execute(), errorHandler1, logger1));
    this.errorHandler = errorHandler;
  }

  @Override
  Completes<Response> execute(Request request, Action.MappedParameters mappedParameters, Logger logger) {
   return executor.execute(request,
                           DefaultMediaTypeMapper.instance(),
                           errorHandler, logger);
  }

}

class RequestHandlerFake extends RequestHandler {

  ParamExecutor0 executor;

  RequestHandlerFake(final Method method, final String path, final List<ParameterResolver<?>> parameterResolvers) {
    super(method, path, parameterResolvers);
    executor = ((request, mediaTypeMapper1, errorHandler1, logger1) ->
      RequestExecutor.executeRequest(() -> Completes.withSuccess(Response.of(Ok)), errorHandler1, logger1));
  }

  RequestHandlerFake(Method method, String path,
                     List<ParameterResolver<?>> parameterResolvers,
                     Handler0 handler) {
    super(method, path, parameterResolvers);
    executor = ((request, mediaTypeMapper1, errorHandler1, logger1) ->
      RequestExecutor.executeRequest(handler::execute, errorHandler1, logger1));
  }

  @Override
  Completes<Response> execute(final Request request,
                              final Action.MappedParameters mappedParameters,
                              final Logger logger) {
    throw new UnsupportedOperationException();
  }

  Completes<Response> execute(Request request, ErrorHandler errorHandler, Logger logger) {
    return executor.execute(request, null, errorHandler, logger);
  }

}
