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
import java.util.Arrays;

import static io.vlingo.common.Completes.withSuccess;
import static io.vlingo.http.Response.Status.Ok;
import static io.vlingo.http.Response.of;
import static io.vlingo.http.resource.ParameterResolver.path;
import static io.vlingo.http.resource.ParameterResolver.query;
import static io.vlingo.http.resource.serialization.JsonSerialization.serialized;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RequestHandler5Test extends RequestHandlerTestBase {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void handlerWithOneParam() {
    final RequestHandler5<String, String, String, Integer, Integer> handler = new RequestHandler5<>(
      Method.GET,
      "/posts/{postId}/comment/{commentId}/user/{userId}",
      path(0, String.class),
      path(1, String.class),
      path(2, String.class),
      query("page", Integer.class, 10),
      query("pageSize", Integer.class, 10)
    ).handle((postId, commentId, userId, page, pageSize) -> withSuccess(of(Ok, serialized(postId + " " + commentId))));

    final Response response = handler.execute("my-post", "my-comment", "admin", 10, 10).outcome();

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

    final RequestHandler5<String, String, String, Integer, Integer> handler = new RequestHandler5<>(
      Method.GET,
      "/posts/{postId}/comment/{commentId}/user/{userId}",
      path(0, String.class),
      path(1, String.class),
      path(2, String.class),
      query("page", Integer.class, 10),
      query("pageSize", Integer.class, 10)
    );
    handler.execute("my-post", "my-comment", "admin", 10, 10);
  }

  @Test
  public void actionSignature() {
    final RequestHandler5<String, String, String, Integer, Integer> handler = new RequestHandler5<>(
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
      .and(URI.create("/posts/my-post/comments/my-comments"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Arrays.asList(
        new Action.MappedParameter("String", "my-post"),
        new Action.MappedParameter("String", "my-comment"),
        new Action.MappedParameter("String", "my-user"))
      );
    final RequestHandler5<String, String, String, Integer, Integer> handler = new RequestHandler5<>(
      Method.GET,
      "/posts/{postId}/comment/{commentId}/user/{userId}",
      path(0, String.class),
      path(1, String.class),
      path(2, String.class),
      query("page", Integer.class, 10),
      query("pageSize", Integer.class, 10)
    )
      .handle((postId, commentId, userId, page, pageSize) -> withSuccess(of(Ok, serialized(postId + " " + commentId))));
    final Response response = handler.execute(request, mappedParameters).outcome();

    assertResponsesAreEquals(of(Ok, serialized("my-post my-comment")), response);
  }
}
