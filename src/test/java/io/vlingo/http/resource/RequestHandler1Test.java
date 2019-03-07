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

public class RequestHandler1Test extends RequestHandlerTestBase {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private <T> RequestHandler1<T> createRequestHandler(Method method, String path, ParameterResolver<T> parameterResolver) {
    return new RequestHandler1<>(Method.GET,
      path,
      parameterResolver,
      ErrorHandler.handleAllWith(InternalServerError));
  }
  
  @Test
  public void handlerWithOneParam() {
    final RequestHandler1<String> handler = createRequestHandler(
      Method.GET,
      "/posts/{postId}",
      path(0, String.class)
    ).handle((postId) -> withSuccess(of(Ok, serialized(postId))));

    final Response response = handler.execute("my-post", logger).outcome();

    assertNotNull(handler);
    assertEquals(Method.GET, handler.method);
    assertEquals("/posts/{postId}", handler.path);
    assertEquals(String.class, handler.resolver.paramClass);
    assertResponsesAreEquals(of(Ok, serialized("my-post")), response);
  }
  
  @Test()
  public void throwExceptionWhenNoHandlerIsDefined() {
    thrown.expect(HandlerMissingException.class);
    thrown.expectMessage("No handle defined for GET /posts/{postId}");

    final RequestHandler1<String> handler = createRequestHandler(
      Method.GET,
      "/posts/{postId}",
      path(0, String.class)
    ).handle(null);
    handler.execute("my-post", logger);
  }

  @Test
  public void errorHandlerInvoked() {
    final RequestHandler1<String> handler = createRequestHandler(Method.GET, "/posts/{postId}", path(0, String.class))
      .handle((param) -> {
        throw new RuntimeException("Test Handler exception");
      })
      .onError(
        (error) -> Completes.withSuccess(Response.of(Response.Status.Imateapot))
      );
    Completes<Response> responseCompletes = handler.execute("idVal1", logger);
    assertResponsesAreEquals(Response.of(Imateapot), responseCompletes.await());
  }

  @Test
  public void actionSignature() {
    final RequestHandler1<String> handler = createRequestHandler(Method.GET, "/posts/{postId}", path(0, String.class))
      .handle((postId) -> withSuccess(of(Ok, serialized(postId))));

    assertEquals("String postId", handler.actionSignature);
  }

  @Test
  public void actionSignatureWithEmptyParamNameThrowsException() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Empty path parameter name for GET /posts/{}");

    createRequestHandler(Method.GET, "/posts/{}", path(0, String.class))
      .handle((postId) -> withSuccess(of(Ok, serialized(postId))));
  }

  @Test
  public void actionSignatureWithBlankParamNameThrowsException() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Empty path parameter name for GET /posts/{ }");

    createRequestHandler(Method.GET, "/posts/{ }", path(0, String.class))
      .handle((postId) -> withSuccess(of(Ok, serialized(postId))));
  }

  @Test
  public void actionWithoutParamNameShouldNotThrowException() {
    final RequestHandler1<String> handler = createRequestHandler(Method.POST, "/posts", ParameterResolver.body(String.class))
      .handle((postId) -> withSuccess(of(Ok, serialized(postId))));

    assertEquals("", handler.actionSignature);
  }

  @Test
  public void executeWithRequestAndMappedParameters() {
    final Request request = Request.has(Method.GET)
      .and(URI.create("/posts/my-post"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Collections.singletonList(
        new Action.MappedParameter("String", "my-post"))
      );
    final RequestHandler1<String> handler = createRequestHandler(Method.GET, "/posts/{postId}", path(0, String.class))
      .handle((postId) -> withSuccess(of(Ok, serialized("it is " + postId))));
    final Response response = handler.execute(request, mappedParameters, logger).outcome();

    assertResponsesAreEquals(of(Ok, serialized("it is my-post")), response);
  }

  @Test
  public void executeWithRequestAndMappedParametersWithWrongSignatureType() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Value my-post is of type String instead of Integer");

    final Request request = Request.has(Method.GET)
      .and(URI.create("/posts/my-post"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Collections.singletonList(
        new Action.MappedParameter("String", "my-post"))
      );
    final RequestHandler1<Integer> handler =
      createRequestHandler(Method.GET, "/posts/{postId}", path(0, Integer.class))
        .handle((postId) -> withSuccess(of(Ok, serialized("it is my-post"))));

    handler.execute(request, mappedParameters, logger);
  }


  //region adding handlers to RequestHandler0

  @Test
  public void addingHandlerParam() {
    final Request request = Request.has(Method.GET)
      .and(URI.create("/user/admin/picture/2"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Arrays.asList(
        new Action.MappedParameter("String", "admin"),
        new Action.MappedParameter("Integer", 1))
      );

    final RequestHandler2<String, Integer> handler =
      createRequestHandler(Method.GET, "/user/{userId}/picture/{pictureId}", path(0, String.class))
        .param(Integer.class);

    assertResolvesAreEquals(path(1, Integer.class), handler.resolverParam2);
    assertEquals((Integer) 1, handler.resolverParam2.apply(request, mappedParameters));
  }

  @Test
  public void addingHandlerBody() {
    final Request request = Request.has(Method.POST)
      .and(URI.create("/user/admin/name"))
      .and(Body.from("{\"given\":\"John\",\"family\":\"Doe\"}"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Collections.singletonList(
        new Action.MappedParameter("String", "admin"))
      );

    final RequestHandler2<String, NameData> handler =
      createRequestHandler(Method.GET, "/user/{userId}/picture/{pictureId}", path(0, String.class))
        .body(NameData.class);

    assertResolvesAreEquals(body(NameData.class), handler.resolverParam2);
    assertEquals(new NameData("John", "Doe"), handler.resolverParam2.apply(request, mappedParameters));
  }

  @Test
  public void addingHandlerBodyWithMapper() {
    final Request request = Request.has(Method.POST)
                                   .and(URI.create("/user/admin/name"))
                                   .and(Body.from("{\"given\":\"John\",\"family\":\"Doe\"}"))
                                   .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Collections.singletonList(
        new Action.MappedParameter("String", "admin"))
      );

    final RequestHandler2<String, NameData> handler =
      createRequestHandler(Method.GET, "/user/{userId}/picture/{pictureId}", path(0, String.class))
        .body(NameData.class, TestMapper.class);

    assertResolvesAreEquals(body(NameData.class), handler.resolverParam2);
    assertEquals(new NameData("John", "Doe"), handler.resolverParam2.apply(request, mappedParameters));
  }

  @Test
  public void addingHandlerQuery() {
    final Request request = Request.has(Method.GET)
      .and(URI.create("/user/admin?filter=abc"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Collections.emptyList());

    final RequestHandler2<String, String> handler =
      createRequestHandler(Method.GET, "/user/{userId}", path(0, String.class))
        .query("filter");

    assertResolvesAreEquals(query("filter", String.class), handler.resolverParam2);
    assertEquals("abc", handler.resolverParam2.apply(request, mappedParameters));
  }


  @Test
  public void addingHandlerHeader() {
    final RequestHeader hostHeader = RequestHeader.of("Host", "www.vlingo.io");
    final Request request = Request.has(Method.GET)
      .and(URI.create("/user/admin"))
      .and(Header.Headers.of(hostHeader))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Collections.emptyList());

    final RequestHandler2<String, Header> handler =
      createRequestHandler(Method.GET, "/user/{userId}", path(0, String.class))
      .header("Host");

    assertResolvesAreEquals(header("Host"), handler.resolverParam2);
    assertEquals(hostHeader, handler.resolverParam2.apply(request, mappedParameters));
  }

  //endregion
}
