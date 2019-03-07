/*
 * Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.resource;

import io.vlingo.common.Completes;
import io.vlingo.http.*;
import io.vlingo.http.sample.user.NameData;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import static io.vlingo.common.Completes.withSuccess;
import static io.vlingo.http.Response.Status.*;
import static io.vlingo.http.Response.of;
import static io.vlingo.http.resource.ParameterResolver.*;
import static io.vlingo.http.resource.serialization.JsonSerialization.serialized;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RequestHandler4Test extends RequestHandlerTestBase {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private <T, R, U, I> RequestHandler4<T, R, U, I> createRequestHandler(Method method, String path,
                                                                     ParameterResolver<T> parameterResolver1,
                                                                     ParameterResolver<R> parameterResolver2,
                                                                     ParameterResolver<U> parameterResolver3,
                                                                     ParameterResolver<I> parameterResolver4) {
    return new RequestHandler4<>(
      method,
      path,
      parameterResolver1,
      parameterResolver2,
      parameterResolver3,
      parameterResolver4,
      ErrorHandler.handleAllWith(InternalServerError));
  }

  @Test
  public void handlerWithOneParam() {
    final RequestHandler4<String, String, String, Integer> handler = createRequestHandler(
      Method.GET,
      "/posts/{postId}/comment/{commentId}/user/{userId}",
      path(0, String.class),
      path(1, String.class),
      path(2, String.class),
      query("page", Integer.class, 10)
    ).handle((postId, commentId, userId, page) -> withSuccess(of(Ok, serialized(postId + " " + commentId))));

    final Response response = handler.execute("my-post", "my-comment", "admin", null, logger).outcome();

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
    thrown.expectMessage("No handle defined for GET /posts/{postId}");

    final RequestHandler4<String, String, String, Integer> handler = createRequestHandler(
      Method.GET,
      "/posts/{postId}/comment/{commentId}/user/{userId}",
      path(0, String.class),
      path(1, String.class),
      path(2, String.class),
      query("page", Integer.class, 10)
    );
    handler.execute("my-post", "my-comment", "admin", null, logger);
  }

  @Test
  public void errorHandlerInvoked() {
    final RequestHandler4<String, String, String, Integer> handler = createRequestHandler(
      Method.GET,
      "/posts/{postId}/comment/{commentId}/user/{userId}",
      path(0, String.class),
      path(1, String.class),
      path(2, String.class),
      query("page", Integer.class, 10))
      .handle((param1, param2, param3, param4) -> { throw new RuntimeException("Test Handler exception"); })
      .onError(
        (error) -> Completes.withSuccess(Response.of(Response.Status.Imateapot))
      );
    Completes<Response> responseCompletes = handler.execute("idVal1", "idVal2", "idVal3", 1, logger);
    assertResponsesAreEquals(Response.of(Imateapot), responseCompletes.await());
  }

  @Test
  public void actionSignature() {
    final RequestHandler4<String, String, String, Integer> handler = createRequestHandler(
      Method.GET,
      "/posts/{postId}/comment/{commentId}/user/{userId}",
      path(0, String.class),
      path(1, String.class),
      path(2, String.class),
      query("page", Integer.class, 10)
    );

    assertEquals("String postId, String commentId, String userId", handler.actionSignature);
  }

  @Test
  public void executeWithRequestAndMappedParameters() {
    final Request request = Request.has(Method.GET)
      .and(URI.create("/posts/my-post/comments/my-comments"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Arrays.asList(
        new Action.MappedParameter("String", "my-post"),
        new Action.MappedParameter("String", "my-comment"),
        new Action.MappedParameter("String", "my-user"))
      );
    final RequestHandler4<String, String, String, Integer> handler = createRequestHandler(
      Method.GET,
      "/posts/{postId}/comment/{commentId}/user/{userId}",
      path(0, String.class),
      path(1, String.class),
      path(2, String.class),
      query("page", Integer.class, 10)
    )
      .handle((postId, commentId, userId, page) -> withSuccess(of(Ok, serialized(postId + " " + commentId))));
    final Response response = handler.execute(request, mappedParameters, logger).outcome();

    assertResponsesAreEquals(of(Ok, serialized("my-post my-comment")), response);
  }

  //region adding handlers to RequestHandler0

  @Test
  public void addingHandlerParam() {
    final Request request = Request.has(Method.GET)
      .and(URI.create("/posts/my-post/comment/my-comment/votes/10/user/admin"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Arrays.asList(
        new Action.MappedParameter("String", "my-post"),
        new Action.MappedParameter("String", "my-comment"),
        new Action.MappedParameter("String", 10),
        new Action.MappedParameter("String", "admin"),
        new Action.MappedParameter("String", "justanother"))
      );

    final RequestHandler5<String, String, Integer, String, String> handler =
      createRequestHandler(
        Method.GET,
        "/posts/{postId}/comment/{commentId}/votes/{votesNumber}/user/{userId}/{another}",
        path(0, String.class),
        path(1, String.class),
        path(2, Integer.class),
        path(3, String.class)
      )
        .param(String.class);

    assertResolvesAreEquals(path(4, String.class), handler.resolverParam5);
    assertEquals("justanother", handler.resolverParam5.apply(request, mappedParameters));
  }

  @Test
  public void addingHandlerBody() {
    final Request request = Request.has(Method.POST)
      .and(URI.create("/posts/my-post/comment/my-comment"))
      .and(Body.from("{\"given\":\"John\",\"family\":\"Doe\"}"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.POST, "ignored", Arrays.asList(
        new Action.MappedParameter("String", "my-post"),
        new Action.MappedParameter("String", "my-comment"))
      );

    final RequestHandler5<String, String, Integer, Integer, NameData> handler =
      createRequestHandler(
        Method.POST,
        "/posts/{postId}/comment/{commentId}/votes/{votesNumber}",
        path(0, String.class),
        path(1, String.class),
        path(2, Integer.class),
        query("page", Integer.class, 10)
      )
        .body(NameData.class);

    assertResolvesAreEquals(body(NameData.class), handler.resolverParam5);
    assertEquals(new NameData("John", "Doe"), handler.resolverParam5.apply(request, mappedParameters));
  }

  @Test
  public void addingHandlerBodyWithMapper() {
    final Request request = Request.has(Method.POST)
      .and(URI.create("/posts/my-post/comment/my-comment"))
      .and(Body.from("{\"given\":\"John\",\"family\":\"Doe\"}"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.POST, "ignored", Arrays.asList(
        new Action.MappedParameter("String", "my-post"),
        new Action.MappedParameter("String", "my-comment"))
      );

    final RequestHandler5<String, String, Integer, Integer, NameData> handler =
      createRequestHandler(
        Method.POST,
        "/posts/{postId}/comment/{commentId}/votes/{votesNumber}",
        path(0, String.class),
        path(1, String.class),
        path(2, Integer.class),
        query("page", Integer.class, 10)
      )
        .body(NameData.class, TestMapper.class);

    assertResolvesAreEquals(body(NameData.class), handler.resolverParam5);
    assertEquals(new NameData("John", "Doe"), handler.resolverParam5.apply(request, mappedParameters));
  }

  @Test
  public void addingHandlerQuery() {
    final Request request = Request.has(Method.POST)
      .and(URI.create("/posts/my-post/comment/my-comment?filter=abc"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Collections.emptyList());

    final RequestHandler5<String, String, Integer, Integer, String> handler =
      createRequestHandler(
        Method.GET,
        "/posts/{postId}/comment/{commentId}/votes/{votesId}",
        path(0, String.class),
        path(1, String.class),
        path(2, Integer.class),
        query("page", Integer.class, 10)
      )
        .query("filter");

    assertResolvesAreEquals(query("filter", String.class), handler.resolverParam5);
    assertEquals("abc", handler.resolverParam5.apply(request, mappedParameters));
  }


  @Test
  public void addingHandlerHeader() {
    final RequestHeader hostHeader = RequestHeader.of("Host", "www.vlingo.io");
    final Request request = Request.has(Method.GET)
      .and(URI.create("/posts/my-post/comment/my-comment"))
      .and(Header.Headers.of(hostHeader))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Collections.emptyList());

    final RequestHandler5<String, String, Integer, Integer, Header> handler =
      createRequestHandler(
        Method.GET,
        "/posts/{postId}/comment/{commentId}/votes/{votesNumber}",
        path(0, String.class),
        path(1, String.class),
        path(2, Integer.class),
        query("page", Integer.class, 10)
      )
        .header("Host");

    assertResolvesAreEquals(header("Host"), handler.resolverParam5);
    assertEquals(hostHeader, handler.resolverParam5.apply(request, mappedParameters));
  }

  //endregion
}
