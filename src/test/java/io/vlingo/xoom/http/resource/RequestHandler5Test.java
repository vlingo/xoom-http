/*
 * Copyright © 2012-2022 VLINGO LABS. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.xoom.http.resource;

import static io.vlingo.xoom.common.Completes.withSuccess;
import static io.vlingo.xoom.http.Response.of;
import static io.vlingo.xoom.http.Response.Status.InternalServerError;
import static io.vlingo.xoom.http.Response.Status.Ok;
import static io.vlingo.xoom.http.resource.ParameterResolver.path;
import static io.vlingo.xoom.http.resource.ParameterResolver.query;
import static io.vlingo.xoom.http.resource.serialization.JsonSerialization.serialized;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.vlingo.xoom.http.Method;
import io.vlingo.xoom.http.Request;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.Version;
import io.vlingo.xoom.http.resource.RequestHandler5.Handler5;

public class RequestHandler5Test extends RequestHandlerTestBase {
  @Rule
  @SuppressWarnings("deprecation")
  public ExpectedException thrown = ExpectedException.none();

  private <T, R, U, I, J> RequestHandler5<T, R, U, I, J> createRequestHandler(Method method,
                                                                              String path,
                                                                              ParameterResolver<T> parameterResolver1,
                                                                              ParameterResolver<R> parameterResolver2,
                                                                              ParameterResolver<U> parameterResolver3,
                                                                              ParameterResolver<I> parameterResolver4,
                                                                              ParameterResolver<J> parameterResolver5) {
    return new RequestHandler5<>(
      method,
      path,
      parameterResolver1,
      parameterResolver2,
      parameterResolver3,
      parameterResolver4,
      parameterResolver5,
      ErrorHandler.handleAllWith(InternalServerError),
      DefaultMediaTypeMapper.instance());
  }

  @Test
  public void handlerWithOneParam() {
    final RequestHandler5<String, String, String, Integer, Integer> handler = createRequestHandler(
      Method.GET,
      "/posts/{postId}/comment/{commentId}/user/{userId}",
      path(0, String.class),
      path(1, String.class),
      path(2, String.class),
      query("page", Integer.class, 10),
      query("pageSize", Integer.class, 10)
    ).handle((Handler5<String, String, String, Integer, Integer>) (postId, commentId, userId, page, pageSize) ->
      withSuccess(of(Ok, serialized(postId + " " + commentId))));

    final Response response = handler.execute(Request.method(Method.GET), "my-post", "my-comment", "admin", 10, 10, logger).outcome();

    assertNotNull(handler);
    assertEquals(Method.GET, handler.method);
    assertEquals("/posts/{postId}/comment/{commentId}/user/{userId}", handler.path);
    assertEquals(String.class, handler.resolverParam1.paramClass);
    assertEquals(String.class, handler.resolverParam2.paramClass);
    assertResponsesAreEquals(of(Ok, serialized("my-post my-comment")), response);
  }

  @Test()
  public void throwExceptionWhenNoHandlerIsDefined() {
    thrown.expect(HandlerMissingException.class);
    thrown.expectMessage("No handler defined for GET /posts/{postId}/comment/{commentId}/user/{userId}");

    final RequestHandler5<String, String, String, Integer, Integer> handler = createRequestHandler(
      Method.GET,
      "/posts/{postId}/comment/{commentId}/user/{userId}",
      path(0, String.class),
      path(1, String.class),
      path(2, String.class),
      query("page", Integer.class, 10),
      query("pageSize", Integer.class, 10)
    );
    handler.execute(Request.method(Method.GET), "my-post", "my-comment", "admin", 10, 10, logger);
  }

  @Test
  public void actionSignature() {
    final RequestHandler5<String, String, String, Integer, Integer> handler = createRequestHandler(
      Method.GET,
      "/posts/{postId}/comment/{commentId}/user/{userId}",
      path(0, String.class),
      path(1, String.class),
      path(2, String.class),
      query("page", Integer.class, 10),
      query("pageSize", Integer.class, 10)
    );

    assertEquals("String postId, String commentId, String userId", handler.actionSignature);
  }

  @Test
  public void executeWithRequestAndMappedParameters() {
    final Request request = Request.has(Method.GET)
      .and(URI.create("/posts/my-post/comments/my-comment/users/my-user"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Arrays.asList(
        new Action.MappedParameter("String", "my-post"),
        new Action.MappedParameter("String", "my-comment"),
        new Action.MappedParameter("String", "my-user"))
      );
    final RequestHandler5<String, String, String, Integer, Integer> handler = createRequestHandler(
      Method.GET,
      "/posts/{postId}/comments/{commentId}/users/{userId}",
      path(0, String.class),
      path(1, String.class),
      path(2, String.class),
      query("page", Integer.class, 10),
      query("pageSize", Integer.class, 10)
    )
      .handle((Handler5<String, String, String, Integer, Integer>) (postId, commentId, userId, page, pageSize)
        -> withSuccess(of(Ok, serialized(postId + " " + commentId))));

    final Response response = handler.execute(request, mappedParameters, logger).outcome();

    assertResponsesAreEquals(of(Ok, serialized("my-post my-comment")), response);
  }
}
