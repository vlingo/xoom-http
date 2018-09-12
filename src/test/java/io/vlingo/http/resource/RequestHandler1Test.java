/*
 * Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.resource;

import io.vlingo.actors.Completes;
import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.Response;
import io.vlingo.http.Version;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.util.Collections;

import static io.vlingo.http.Response.Status.Ok;
import static io.vlingo.http.resource.serialization.JsonSerialization.serialized;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RequestHandler1Test {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void handlerWithOneParam() {
    final RequestHandler1<String> handler = new RequestHandler1<>(
      Method.GET,
      "/posts/{postId}",
      ParameterResolver.path(0, String.class)
    ).handle((postId) -> Completes.withSuccess(Response.of(Ok, serialized(postId))));

    assertNotNull(handler);
    assertEquals(Method.GET, handler.method);
    assertEquals("/posts/{postId}", handler.path);
    assertEquals(String.class, handler.resolver.paramClass);
    assertEquals(Response.of(Ok, serialized("my-post")).toString(), handler.execute("my-post").outcome().toString());
  }

  @Test()
  public void throwExceptionWhenNoHandlerIsDefined() {
    thrown.expect(HandlerMissingException.class);
    thrown.expectMessage("No handle defined for GET /posts/{postId}");

    final RequestHandler1<String> handler = new RequestHandler1<>(Method.GET, "/posts/{postId}", ParameterResolver.path(0, String.class));
    final MockCompletesEventuallyResponse completes = new MockCompletesEventuallyResponse();
    handler.execute("my-post");
  }

  @Test
  public void convertingRequestHandler0ToRequestHandler1() {
    final RequestHandler1<String> handler = new RequestHandler0(Method.GET, "/posts/{postId}")
      .param(String.class)
      .handle((postId) -> Completes.withSuccess((Response.of(Ok, serialized(postId)))));

    assertNotNull(handler);
    assertEquals(Method.GET, handler.method);
    assertEquals("/posts/{postId}", handler.path);
    assertEquals(String.class, handler.resolver.paramClass);
    assertEquals(Response.of(Ok, serialized("my-post")).toString(), handler.execute("my-post").outcome().toString());
  }

  @Test
  public void actionSignature() {
    final RequestHandler1<String> handler = new RequestHandler1<>(Method.GET, "/posts/{postId}", ParameterResolver.path(0, String.class))
      .handle((postId) -> Completes.withSuccess(Response.of(Ok, serialized(postId))));

    assertEquals("String postId", handler.actionSignature);
  }

  @Test
  public void actionSignatureWithEmptyParamNameThrowsException() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Empty path parameter name for GET /posts/{}");

    new RequestHandler1<>(Method.GET, "/posts/{}", ParameterResolver.path(0, String.class))
      .handle((postId) -> Completes.withSuccess(Response.of(Ok, serialized(postId))));
  }

  @Test
  public void actionSignatureWithBlankParamNameThrowsException() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Empty path parameter name for GET /posts/{ }");

    new RequestHandler1<>(Method.GET, "/posts/{ }", ParameterResolver.path(0, String.class))
      .handle((postId) -> Completes.withSuccess(Response.of(Ok, serialized(postId))));
  }

  @Test
  public void actionWithoutParamNameShouldNotThrowException() {
    final RequestHandler1<String> handler = new RequestHandler1<>(Method.POST, "/posts", ParameterResolver.body(String.class))
      .handle((postId) -> Completes.withSuccess(Response.of(Ok, serialized(postId))));

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
    final Response response = Response.of(Ok, serialized("it is my-post"));
    final RequestHandler1<String> handler = new RequestHandler1<>(Method.GET, "/posts/{postId}", ParameterResolver.path(0, String.class))
      .handle((postId) -> Completes.withSuccess(response));
    assertEquals(response, handler.execute(request, mappedParameters).outcome());
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
    final RequestHandler1<Integer> handler = new RequestHandler1<>(
      Method.GET,
      "/posts/{postId}",
      ParameterResolver.path(0, Integer.class)
      ).handle((postId) -> Completes.withSuccess(Response.of(Ok, serialized("it is my-post"))));

    handler.execute(request, mappedParameters);
  }
}
