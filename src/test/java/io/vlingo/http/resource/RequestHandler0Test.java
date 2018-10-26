/*
 * Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.resource;

import io.vlingo.http.*;
import io.vlingo.http.sample.user.NameData;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.util.Collections;

import static io.vlingo.common.Completes.withSuccess;
import static io.vlingo.http.Response.Status.Created;
import static io.vlingo.http.Response.Status.Ok;
import static io.vlingo.http.Response.of;
import static io.vlingo.http.resource.serialization.JsonSerialization.serialized;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RequestHandler0Test extends RequestHandlerTestBase {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void simpleHandler() {
    final RequestHandler0 handler = new RequestHandler0(Method.GET, "/helloworld")
      .handle(() -> withSuccess(of(Created)));
    final Response response = handler.execute().outcome();

    assertNotNull(handler);
    assertEquals(Method.GET, handler.method);
    assertEquals("/helloworld", handler.path);
    assertResponsesAreEquals(of(Created), response);
  }

  @Test
  public void throwExceptionWhenNoHandlerIsDefined() {
    thrown.expect(HandlerMissingException.class);
    thrown.expectMessage("No handle defined for GET /helloworld");

    final RequestHandler0 handler = new RequestHandler0(Method.GET, "/helloworld");

    handler.execute();
  }

  @Test
  public void actionSignatureIsEmpty() {
    final RequestHandler0 handler = new RequestHandler0(Method.GET, "/helloworld")
      .handle(() -> withSuccess(of(Created)));

    assertEquals("", handler.actionSignature);
  }

  @Test
  public void executeWithRequestAndMappedParametersHasToReturnTheSameAsExecute() {
    final Request request = Request.has(Method.GET)
      .and(URI.create("/hellworld"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Collections.emptyList());

    final RequestHandler0 handler = new RequestHandler0(Method.GET, "/helloworld")
      .handle(() -> withSuccess(of(Created)));
    final Response response = handler.execute(request, mappedParameters).outcome();

    assertNotNull(handler);
    assertEquals(Method.GET, handler.method);
    assertEquals("/helloworld", handler.path);
    assertResponsesAreEquals(of(Created), response);
  }

  @Test
  public void executeWithParam() {
    final Request request = Request.has(Method.GET)
      .and(URI.create("/user/admin"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1,
                                  Method.GET,
                      "ignored()",
                                  Collections.singletonList(new Action.MappedParameter("String", "admin")));

    final RequestHandler1<String> handler = new RequestHandler0(Method.GET, "/user/{userId}")
      .param(String.class)
      .handle((userId) -> withSuccess(of(Ok, serialized(userId))));
    final Response response = handler.execute(request, mappedParameters).outcome();

    assertResponsesAreEquals(of(Ok, serialized("admin")), response);
  }

  @Test
  public void executeWithBody() {
    final Request request = Request.has(Method.POST)
      .and(URI.create("/user/admin/name"))
      .and(Version.Http1_1)
      .and(Body.from("{\"given\":\"John\",\"family\":\"Doe\"}"));
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.POST, "ignored", Collections.emptyList());

    final RequestHandler1<NameData> handler = new RequestHandler0(Method.GET, "/user/admin/name")
      .body(NameData.class)
      .handle((nameData) -> withSuccess(of(Ok, serialized(nameData))));
    final Response response = handler.execute(request, mappedParameters).outcome();

    assertResponsesAreEquals(of(Ok, serialized(new NameData("John", "Doe"))), response);
  }

  @Test
  public void executeWithQuery() {
    final Request request = Request.has(Method.POST)
      .and(URI.create("/user?filter=name"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Collections.emptyList());

    final RequestHandler1<String> handler = new RequestHandler0(Method.GET, "/user")
      .query("filter")
      .handle((filter) -> withSuccess(of(Ok, serialized(filter))));
    final Response response = handler.execute(request, mappedParameters).outcome();

    assertResponsesAreEquals( of(Ok, serialized("name")), response);
  }


  @Test
  public void executeWitHeader() {
    final RequestHeader hostHeader = RequestHeader.of("Host", "www.vlingo.io");
    final Request request = Request.has(Method.GET)
      .and(URI.create("/user"))
      .and(Header.Headers.of(hostHeader))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Collections.emptyList());

    final RequestHandler1<Header> handler = new RequestHandler0(Method.GET, "/user")
      .header("Host")
      .handle((host) -> withSuccess(of(Ok, serialized(host))));
    final Response response = handler.execute(request, mappedParameters).outcome();

    assertResponsesAreEquals(of(Ok, serialized(hostHeader)), response);
  }
}
