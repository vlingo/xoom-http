/*
 * Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.resource;

import io.vlingo.http.Response;
import org.junit.Test;

import static io.vlingo.http.Response.Status.Ok;
import static io.vlingo.http.resource.serialization.JsonSerialization.serialized;
import static org.junit.Assert.*;

import static io.vlingo.http.resource.ResourceBuilder.*;

public class ResourceBuilderTest {

  @Test
  public void simpleRoute() {
    final DynamicResource resource = (DynamicResource) resource("userResource",
        get("/helloWorld").handle(() -> Response.of(Ok, serialized("Hello World"))),
        post("/post/{postId}").param1(String.class).handle((postId) -> Response.of(Ok, serialized(postId)))
      );

    assertNotNull(resource);
    assertEquals("userResource", resource.name);
    assertEquals(10, resource.handlerPoolSize);
    assertEquals(2, resource.handlers.size());
  }

}
