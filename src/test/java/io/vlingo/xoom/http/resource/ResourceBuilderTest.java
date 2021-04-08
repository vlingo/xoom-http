/*
 * Copyright © 2012-2020 VLINGO LABS. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.xoom.http.resource;

import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.http.Method;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.sample.user.UserData;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static io.vlingo.xoom.http.Response.Status.Ok;
import static io.vlingo.xoom.http.resource.ResourceBuilder.*;
import static io.vlingo.xoom.http.resource.serialization.JsonSerialization.serialized;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ResourceBuilderTest extends ResourceTestFixtures {

  @Test
  public void simpleRoute() {
    final DynamicResource resource = (DynamicResource) resource("userResource",
        get("/helloWorld").handle(() -> Completes.withSuccess((Response.of(Ok, serialized("Hello World"))))),
        post("/post/{postId}")
          .param(String.class)
          .body(UserData.class)
          .handle((RequestHandler2.Handler2<String, UserData>) (postId, userData) -> Completes.withSuccess(Response.of(Ok, serialized(postId))))
      );

    assertNotNull(resource);
    assertEquals("userResource", resource.name);
    assertEquals(10, resource.handlerPoolSize);
    assertEquals(2, resource.handlers.size());
  }

  @Test
  public void shouldRespondToCorrectResourceHandler() throws URISyntaxException {
    final DynamicResource resource = (DynamicResource) resource("userResource",
      get("/customers/{customerId}/accounts/{accountId}")
        .param(String.class)
        .param(String.class)
        .handle((RequestHandler2.Handler2<String, String>) (customerId, accountID) -> Completes.withSuccess((Response.of(Ok, serialized("users"))))),
      get("/customers/{customerId}/accounts/{accountId}/withdraw")
        .param(String.class)
        .param(String.class)
        .handle((RequestHandler2.Handler2<String, String>) (customerId, accountID) -> Completes.withSuccess((Response.of(Ok, serialized("user admin")))))
    );

    final Action.MatchResults matchWithdrawResource = resource.matchWith(
      Method.GET,
      new URI("/customers/cd1234/accounts/ac1234/withdraw"));
    final Action.MatchResults matchAccountResource = resource.matchWith(
      Method.GET,
      new URI("/customers/cd1234/accounts/ac1234"));

    assertEquals("/customers/{customerId}/accounts/{accountId}/withdraw", matchWithdrawResource.action.uri);
    assertEquals("/customers/{customerId}/accounts/{accountId}", matchAccountResource.action.uri);
  }

}
