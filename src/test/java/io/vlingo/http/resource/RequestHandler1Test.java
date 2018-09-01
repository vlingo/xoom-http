/*
 * Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.resource;

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
      String.class,
      ((request, mappedParameters) -> "my-post")
    ).handle(postId -> Response.of(Ok, serialized(postId)));

    assertNotNull(handler);
    assertEquals(Method.GET, handler.method());
    assertEquals("/posts/{postId}", handler.path());
    assertEquals(String.class, handler.param1Class);
    assertEquals(handler.execute("my-post").toString(), Response.of(Ok, serialized("my-post")).toString());
  }

  @Test()
  public void throwExceptionWhenNoHandlerIsDefined() {
    thrown.expect(HandlerMissingException.class);
    thrown.expectMessage("No handle defined for GET /posts/{postId}");

    final RequestHandler1<String> handler = new RequestHandler1<>(Method.GET, "/posts/{postId}", String.class, (request, mappedParameters) -> "my-post");
    handler.execute("my-post");
  }

  @Test
  public void convertingRequestHandler0ToRequestHandler1() {
    final RequestHandler1<String> handler = new RequestHandler0(Method.GET, "/posts/{postId}")
      .param(String.class)
      .handle(postId -> Response.of(Ok, serialized(postId)));

    assertNotNull(handler);
    assertEquals(Method.GET, handler.method());
    assertEquals("/posts/{postId}", handler.path());
    assertEquals(String.class, handler.param1Class);
    assertEquals(handler.execute("my-post").toString(), Response.of(Ok, serialized("my-post")).toString());
  }

  @Test
  public void actionSignature() {
    final RequestHandler1<String> handler = new RequestHandler1<>(Method.GET, "/posts/{postId}", String.class, (request, mappedParameters) -> "ignored")
      .handle(postId -> Response.of(Ok, serialized(postId)));

    assertEquals("String postId", handler.actionSignature());
  }

  @Test
  public void actionSignatureWithEmptyParamNameThrowsException() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Empty path parameter for GET /posts/{}");
    new RequestHandler1<>(Method.GET, "/posts/{}", String.class, (request, mappedParameters) -> "ignored")
      .handle(postId -> Response.of(Ok, serialized(postId)));
  }

  @Test
  public void actionSignatureWithBlankParamNameThrowsException() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Empty path parameter for GET /posts/{ }");

    new RequestHandler1<>(Method.GET, "/posts/{ }", String.class, ((request, mappedParameters) -> "ignored"))
      .handle(postId -> Response.of(Ok, serialized(postId)));
  }

  @Test
  public void actionWithoutParamNameShouldNotThrowException() {
    final RequestHandler1<String> handler = new RequestHandler1<>(Method.GET, "/posts", String.class, (request, mappedParameters) -> "ignored")
      .handle(postId -> Response.of(Ok, serialized(postId)));

    assertEquals("", handler.actionSignature());
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
    final RequestHandler1<String> handler = new RequestHandler1<>(Method.GET, "/posts/{postId}", String.class, ((request1, mappedParameters1) -> "my-post"))
      .handle((postId) -> response);

    assertEquals(response, handler.execute(request, mappedParameters));
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
    final RequestHandler1<Integer> handler = new RequestHandler1<>(Method.GET, "/posts/{postId}", Integer.class, (request1, mappedParameters1) -> {
      Object value = mappedParameters.mapped.get(0).value;
      if (Integer.class.isInstance(value)) {
        return (Integer) value;
      }
      throw new IllegalArgumentException("Value " + value + " is of type " + mappedParameters.mapped.get(0).type + " instead of Integer");
    }).handle((postId) -> Response.of(Ok, serialized("it is my-post")));

    handler.execute(request, mappedParameters);
  }
}
