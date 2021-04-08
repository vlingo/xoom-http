/*
 * Copyright © 2012-2021 VLINGO LABS. All rights reserved.
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
import static io.vlingo.xoom.http.resource.ParameterResolver.body;
import static io.vlingo.xoom.http.resource.ParameterResolver.header;
import static io.vlingo.xoom.http.resource.ParameterResolver.path;
import static io.vlingo.xoom.http.resource.ParameterResolver.query;
import static io.vlingo.xoom.http.resource.serialization.JsonSerialization.serialized;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.vlingo.xoom.http.Body;
import io.vlingo.xoom.http.Header;
import io.vlingo.xoom.http.Method;
import io.vlingo.xoom.http.Request;
import io.vlingo.xoom.http.RequestHeader;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.Version;
import io.vlingo.xoom.http.resource.RequestHandler3.Handler3;
import io.vlingo.xoom.http.sample.user.NameData;

public class RequestHandler3Test extends RequestHandlerTestBase {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private <T, R, U> RequestHandler3<T, R, U> createRequestHandler(Method method, String path,
                                                                  ParameterResolver<T> parameterResolver1,
                                                                  ParameterResolver<R> parameterResolver2,
                                                                  ParameterResolver<U> parameterResolver3) {
    return new RequestHandler3<>(
      method,
      path,
      parameterResolver1,
      parameterResolver2,
      parameterResolver3,
      ErrorHandler.handleAllWith(InternalServerError),
      DefaultMediaTypeMapper.instance());
  }

  @Test
  public void handlerWithOneParam() {
    final RequestHandler3<String, String, Integer> handler = createRequestHandler(
      Method.GET,
      "/posts/{postId}/comment/{commentId}",
      path(0, String.class),
      path(1, String.class),
      query("page", Integer.class, 10)
    ).handle((Handler3<String, String, Integer>) (postId, commentId, page)
      -> withSuccess(of(Ok, serialized(postId + " " + commentId))));

    final Response response = handler.execute(Request.method(Method.GET), "my-post", "my-comment", null, logger).outcome();

    assertNotNull(handler);
    assertEquals(Method.GET, handler.method);
    assertEquals("/posts/{postId}/comment/{commentId}", handler.path);
    assertEquals(String.class, handler.resolverParam1.paramClass);
    assertEquals(String.class, handler.resolverParam2.paramClass);
    assertResponsesAreEquals(of(Ok, serialized("my-post my-comment")), response);
  }

  @Test()
  public void throwExceptionWhenNoHandlerIsDefined() {
    thrown.expect(HandlerMissingException.class);
    thrown.expectMessage("No handler defined for GET /posts/{postId}/comment/{commentId}");

    final RequestHandler3<String, String, Integer> handler = createRequestHandler(
      Method.GET,
      "/posts/{postId}/comment/{commentId}",
      path(0, String.class),
      path(1, String.class),
      query("page", Integer.class, 10)
    );
    handler.execute(Request.method(Method.GET), "my-post", "my-comment", 1, logger);
  }

  @Test
  public void actionSignature() {
    final RequestHandler3<String, String, Integer> handler = createRequestHandler(
      Method.GET,
      "/posts/{postId}/comment/{commentId}",
      path(0, String.class),
      path(1, String.class),
      query("page", Integer.class, 10)
    );

    assertEquals("String postId, String commentId", handler.actionSignature);
  }

  @Test
  public void executeWithRequestAndMappedParameters() {
    final Request request = Request.has(Method.GET)
      .and(URI.create("/posts/my-post/comments/my-comments"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Arrays.asList(
        new Action.MappedParameter("String", "my-post"),
        new Action.MappedParameter("String", "my-comment"))
      );
    final RequestHandler3<String, String, Integer> handler = createRequestHandler(
      Method.GET,
      "/posts/{postId}/comment/{commentId}",
      path(0, String.class),
      path(1, String.class),
      query("page", Integer.class, 10)
    )
      .handle((Handler3<String,String,Integer>)(postId, commentId, page) -> withSuccess(of(Ok, serialized(postId + " " + commentId))));
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
        new Action.MappedParameter("String", "admin"))
      );

    final RequestHandler4<String, String, Integer, String> handler =
      createRequestHandler(
        Method.GET,
        "/posts/{postId}/comment/{commentId}/votes/{votesNumber}/user/{userId}",
        path(0, String.class),
        path(1, String.class),
        path(2, Integer.class)
      )
        .param(String.class);

    assertResolvesAreEquals(path(3, String.class), handler.resolverParam4);
    assertEquals("admin", handler.resolverParam4.apply(request, mappedParameters));
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

    final RequestHandler4<String, String, Integer, NameData> handler =
      createRequestHandler(
        Method.POST,
        "/posts/{postId}/comment/{commentId}",
        path(0, String.class),
        path(1, String.class),
        query("page", Integer.class, 10)
      )
        .body(NameData.class);

    assertResolvesAreEquals(body(NameData.class), handler.resolverParam4);
    assertEquals(new NameData("John", "Doe"), handler.resolverParam4.apply(request, mappedParameters));
  }


  @SuppressWarnings("deprecation")
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

    final RequestHandler4<String, String, Integer, NameData> handler =
      createRequestHandler(
        Method.POST,
        "/posts/{postId}/comment/{commentId}",
        path(0, String.class),
        path(1, String.class),
        query("page", Integer.class, 10)
      )
        .body(NameData.class, TestMapper.class);

    assertResolvesAreEquals(body(NameData.class), handler.resolverParam4);
    assertEquals(new NameData("John", "Doe"), handler.resolverParam4.apply(request, mappedParameters));
  }


  @Test
  public void addingHandlerQuery() {
    final Request request = Request.has(Method.POST)
      .and(URI.create("/posts/my-post/comment/my-comment?filter=abc"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Collections.emptyList());

    final RequestHandler4<String, String, Integer, String> handler =
      createRequestHandler(
        Method.GET,
        "/posts/{postId}/comment/{commentId}",
        path(0, String.class),
        path(1, String.class),
        query("page", Integer.class, 10)
      )
        .query("filter");

    assertResolvesAreEquals(query("filter", String.class), handler.resolverParam4);
    assertEquals("abc", handler.resolverParam4.apply(request, mappedParameters));
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

    final RequestHandler4<String, String, Integer, Header> handler =
      createRequestHandler(
        Method.GET,
        "/posts/{postId}/comment/{commentId}",
        path(0, String.class),
        path(1, String.class),
        query("page", Integer.class, 10)
      )
        .header("Host");

    assertResolvesAreEquals(header("Host"), handler.resolverParam4);
    assertEquals(hostHeader, handler.resolverParam4.apply(request, mappedParameters));
  }

  //endregion
}
