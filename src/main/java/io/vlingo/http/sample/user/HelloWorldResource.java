package io.vlingo.http.sample.user;

import io.vlingo.http.Response;
import io.vlingo.http.resource.Resource;

import static io.vlingo.common.serialization.JsonSerialization.serialized;
import static io.vlingo.http.Response.Ok;
import static io.vlingo.http.resource.ResourceBuilder.route;

public class HelloWorldResource {
  public static final String NAME = "hello-world";

  public String helloWorld() {
    return "Hello World";
  }

  public Resource resourceHandler() {
    return route(NAME)
      .withHandlerPoolSize(10)
      .get("/hello-world", ((request, response) -> {
        /*
        The Request variable paths, cookies and body is done via request variable (not yet implemented)
        something like:
        final String userId = request.stringPathVariable("userId")
        final UserData userData = request.body(mapper(UserData.class));
         */
        response.completes().with(Response.of(Ok, serialized(this.helloWorld())));
      }))
      .build();
  }
}
