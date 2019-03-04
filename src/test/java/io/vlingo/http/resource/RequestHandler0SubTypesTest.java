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
import io.vlingo.common.Outcome;
import io.vlingo.http.Header;
import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.Version;
import io.vlingo.http.Body;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseError;
import io.vlingo.http.RequestHeader;
import io.vlingo.http.sample.user.NameData;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.util.Collections;

import static io.vlingo.common.Completes.withSuccess;
import static io.vlingo.http.Response.Status.Created;
import static io.vlingo.http.Response.Status.Imateapot;
import static io.vlingo.http.resource.ParameterResolver.*;
import static io.vlingo.http.resource.RequestHandler0SubTypes.MyResponse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RequestHandler0SubTypesTest extends RequestHandlerTestBase {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void simpleHandler() {
    final RequestHandler0SubTypes handler = new RequestHandler0SubTypes(Method.GET, "/helloworld")
      .handle(() -> withSuccess(of(Created, "someExtraData")));
    final MyResponse response = handler.execute().outcome().get();

    assertNotNull(handler);
    assertEquals(Method.GET, handler.method);
    assertEquals("/helloworld", handler.path);
    assertEquals("someExtraData", response.extraData);
  }

  @Test
  public void errorHandlerInvoked() {
    final RequestHandler0SubTypes handler = new RequestHandler0SubTypes(Method.GET, "/helloworld")
      .handle(() -> {
        throw new RuntimeException("Test Handler exception");
      })
      .onError(
      error -> Completes.withSuccess(Response.of(Response.Status.Imateapot))
    );
    Completes<Outcome<ResponseError, MyResponse>> responseCompletes = handler.execute();
    assertResponsesAreEquals(Response.of(Imateapot), ResponseError.unwrap(responseCompletes.await()));
  }

  @Test
  public void actionSignatureIsEmpty() {
    final RequestHandler0SubTypes handler = new RequestHandler0SubTypes(Method.GET, "/helloworld")
      .handle(() -> withSuccess(of(Created, "")));

    assertEquals("", handler.actionSignature);
  }

  MyResponse of(Response.Status statusCode, String extraData) {
    return new MyResponse(Version.Http1_1, statusCode, Header.Headers.empty(), Body.from(""), extraData);
  }

  @Test
  public void executeWithRequestAndMappedParametersHasToReturnTheSameAsExecute() {
    final Request request = Request.has(Method.GET)
      .and(URI.create("/hellworld"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Collections.emptyList());

    final RequestHandler0SubTypes handler = new RequestHandler0SubTypes(Method.GET, "/helloworld")
      .handle(() -> withSuccess(of(Created, "someExtraData")));

    final Outcome<ResponseError, MyResponse> response = handler.execute(request, mappedParameters).outcome();

    assertNotNull(handler);
    assertEquals(Method.GET, handler.method);
    assertEquals("/helloworld", handler.path);
    assertEquals("someExtraData", response.get().extraData);
  }

  //region adding handlers to RequestHandler0SubTypes

  @Test
  public void addingHandlerParam() {
    final Request request = Request.has(Method.GET)
      .and(URI.create("/user/admin"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Collections.singletonList(
        new Action.MappedParameter("String", "admin"))
      );

    final RequestHandler1<String> handler = new RequestHandler0SubTypes(Method.GET, "/user/{userId}")
      .param(String.class);

    assertResolvesAreEquals(path(0, String.class), handler.resolver);
    assertEquals("admin", handler.resolver.apply(request, mappedParameters));
  }

  @Test
  public void addingHandlerBody() {
    final Request request = Request.has(Method.POST)
      .and(URI.create("/user/admin/name"))
      .and(Body.from("{\"given\":\"John\",\"family\":\"Doe\"}"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.POST, "ignored", Collections.singletonList(
        new Action.MappedParameter("String", "admin"))
      );

    final RequestHandler1<NameData> handler = new RequestHandler0SubTypes(Method.GET, "/user/admin/name")
      .body(NameData.class);

    assertResolvesAreEquals(body(NameData.class), handler.resolver);
    assertEquals(new NameData("John", "Doe"), handler.resolver.apply(request, mappedParameters));
  }

  @Test
  public void addingHandlerBodyWithMapper() {
    final Request request = Request.has(Method.POST)
                                   .and(URI.create("/user/admin/name"))
                                   .and(Body.from("{\"given\":\"John\",\"family\":\"Doe\"}"))
                                   .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.POST, "ignored", Collections.singletonList(
        new Action.MappedParameter("String", "admin"))
      );

    final RequestHandler1<NameData> handler1 = new RequestHandler0SubTypes(Method.GET, "/user/admin/name")
      .body(NameData.class, TestMapper.class);

    assertResolvesAreEquals(body(NameData.class, new TestMapper()), handler1.resolver);
    assertEquals(new NameData("John", "Doe"), handler1.resolver.apply(request, mappedParameters));
  }

  @Test
  public void addingHandlerQuery() {
    final Request request = Request.has(Method.GET)
      .and(URI.create("/user?filter=abc"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Collections.emptyList());

    final RequestHandler1<String> handler = new RequestHandler0SubTypes(Method.GET, "/user")
      .query("filter");

    assertResolvesAreEquals(query("filter", String.class), handler.resolver);
    assertEquals("abc", handler.resolver.apply(request, mappedParameters));
  }


  @Test
  public void addingHandlerHeader() {
    final RequestHeader hostHeader = RequestHeader.of("Host", "www.vlingo.io");
    final Request request = Request.has(Method.GET)
      .and(URI.create("/user?filter=abc"))
      .and(Header.Headers.of(hostHeader))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Collections.emptyList());

    final RequestHandler1<Header> handler = new RequestHandler0SubTypes(Method.GET, "/user")
      .header("Host");

    assertResolvesAreEquals(header("Host"), handler.resolver);
    assertEquals(hostHeader, handler.resolver.apply(request, mappedParameters));
  }

  //endregion
}
