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
import io.vlingo.http.Response;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
      String.class
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

    final RequestHandler1<String> handler = new RequestHandler1<>(Method.GET, "/posts/{postId}", String.class);
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
}
