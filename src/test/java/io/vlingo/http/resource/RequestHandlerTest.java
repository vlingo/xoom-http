package io.vlingo.http.resource;

import io.vlingo.actors.CompletesEventually;
import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.sample.user.NameData;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RequestHandlerTest {

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

  RequestHandlerFake(Method method, String path, List<ParameterResolver<?>> parameterResolvers) {
    super(method, path, parameterResolvers);
  }

  @Override
  void execute(final Request request, final Action.MappedParameters mappedParameters, final CompletesEventually completes) {
    throw new UnsupportedOperationException();
  }
}
