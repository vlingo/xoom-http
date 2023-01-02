/*
 * Copyright © 2012-2023 VLINGO LABS. All rights reserved.
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
import io.vlingo.xoom.http.resource.RequestHandler1.Handler1;
import io.vlingo.xoom.http.sample.user.NameData;

public class RequestHandler1Test extends RequestHandlerTestBase {

  @Rule
  @SuppressWarnings("deprecation")
  public ExpectedException thrown = ExpectedException.none();

  private <T> RequestHandler1<T> createRequestHandler(Method method, String path, ParameterResolver<T> parameterResolver) {
    return new RequestHandler1<>(Method.GET,
      path,
      parameterResolver,
      ErrorHandler.handleAllWith(InternalServerError),
      DefaultMediaTypeMapper.instance());
  }

  @Test
  public void handlerWithOneParam() {
    final RequestHandler1<String> handler = createRequestHandler(
      Method.GET,
      "/posts/{postId}",
      path(0, String.class)
    ).handle((Handler1<String>)(postId) -> withSuccess(of(Ok, serialized(postId))));

    final Response response = handler.execute(Request.method(Method.GET), "my-post", logger).outcome();

    assertNotNull(handler);
    assertEquals(Method.GET, handler.method);
    assertEquals("/posts/{postId}", handler.path);
    assertEquals(String.class, handler.resolver.paramClass);
    assertResponsesAreEquals(of(Ok, serialized("my-post")), response);
  }

  @Test()
  public void throwExceptionWhenNoHandlerIsDefined() {
    thrown.expect(HandlerMissingException.class);
    thrown.expectMessage("No handler defined for GET /posts/{postId}");

    final RequestHandler1<String> handler = createRequestHandler(
      Method.GET,
      "/posts/{postId}",
      path(0, String.class)
    );
    handler.execute(Request.method(Method.GET), "my-post", logger);
  }

  @Test
  public void actionSignature() {
    final RequestHandler1<String> handler = createRequestHandler(Method.GET, "/posts/{postId}", path(0, String.class))
      .handle((Handler1<String>)(postId) -> withSuccess(of(Ok, serialized(postId))));

    assertEquals("String postId", handler.actionSignature);
  }

  @Test
  public void actionSignatureWithEmptyParamNameThrowsException() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Empty path parameter name for GET /posts/{}");

    createRequestHandler(Method.GET, "/posts/{}", path(0, String.class))
      .handle((Handler1<String>)(postId) -> withSuccess(of(Ok, serialized(postId))));
  }

  @Test
  public void actionSignatureWithBlankParamNameThrowsException() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Empty path parameter name for GET /posts/{ }");

    createRequestHandler(Method.GET, "/posts/{ }", path(0, String.class))
      .handle(((Handler1<String>) (postId) -> withSuccess(of(Ok, serialized(postId)))));
  }

  @Test
  public void actionWithoutParamNameShouldNotThrowException() {
    final RequestHandler1<String> handler = createRequestHandler(Method.POST, "/posts", ParameterResolver.body(String.class))
      .handle(((Handler1<String>)(postId) -> withSuccess(of(Ok, serialized(postId)))));

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
      .handle(((Handler1<String>)(postId) -> withSuccess(of(Ok, serialized("it is " + postId)))));
    final Response response = handler.execute(request, mappedParameters, logger).outcome();

    assertResponsesAreEquals(of(Ok, serialized("it is my-post")), response);
  }

  @Test
  public void executeWithRequestAndMappedParametersWithWrongSignatureType() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Value my-post is of mimeType String instead of Integer");

    final Request request = Request.has(Method.GET)
      .and(URI.create("/posts/my-post"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Collections.singletonList(
        new Action.MappedParameter("String", "my-post"))
      );
    final RequestHandler1<Integer> handler =
      createRequestHandler(Method.GET, "/posts/{postId}", path(0, Integer.class))
        .handle((Handler1<Integer>)(postId) -> withSuccess(of(Ok, serialized("it is my-post"))));

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

  @SuppressWarnings("deprecation")
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
  public void addingHandlerBodyWithMediaTypeMapper() {
    final Request request = Request.has(Method.POST)
                                    .and(URI.create("/user/admin/name"))
                                    .and(Body.from("{\"given\":\"John\",\"family\":\"Doe\"}"))
                                    .and(RequestHeader.of(RequestHeader.ContentType, "application/json"))
                                    .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.POST, "ignored", Collections.singletonList(
        new Action.MappedParameter("String", "admin"))
      );

    final RequestHandler1<NameData> handler1 = new RequestHandler0(Method.GET, "/user/admin/name")
      .body(NameData.class);

    assertResolvesAreEquals(body(NameData.class, DefaultMediaTypeMapper.instance()), handler1.resolver);
    assertEquals(new NameData("John", "Doe"), handler1.resolver.apply(request, mappedParameters));
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
