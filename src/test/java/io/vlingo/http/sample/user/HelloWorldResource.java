/*
 * Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.sample.user;

import io.vlingo.http.Response;
import io.vlingo.http.resource.Resource;

import static io.vlingo.common.serialization.JsonSerialization.serialized;
import static io.vlingo.http.Response.Status.Ok;
import static io.vlingo.http.resource.ResourceBuilder.route;

public class HelloWorldResource {
  public static final String NAME = "hello-world";

  public String helloWorld() {
    return "Hello World";
  }

  public Resource<?> resourceHandler() {
    return route(NAME)
      .withHandlerPoolSize(10)
      .get("/hello-world", request -> {
        /*
        The Request variable paths, cookies and body is done via request variable (not yet implemented)
        something like:
        final String userId = request.stringPathVariable("userId")
        final UserData userData = request.body(mapper(UserData.class));
         */
        return Response.of(Ok, serialized(this.helloWorld()));
      })
      .build();
  }
}
