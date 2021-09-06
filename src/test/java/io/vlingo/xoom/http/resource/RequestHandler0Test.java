/*
 * Copyright © 2012-2021 VLINGO LABS. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.xoom.http.resource;

import static io.vlingo.xoom.common.Completes.withSuccess;
import static io.vlingo.xoom.http.Response.of;
import static io.vlingo.xoom.http.Response.Status.Created;
import static io.vlingo.xoom.http.resource.ParameterResolver.body;
import static io.vlingo.xoom.http.resource.ParameterResolver.header;
import static io.vlingo.xoom.http.resource.ParameterResolver.path;
import static io.vlingo.xoom.http.resource.ParameterResolver.query;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.vlingo.xoom.http.Body;
import io.vlingo.xoom.http.Header;
import io.vlingo.xoom.http.Method;
import io.vlingo.xoom.http.Request;
import io.vlingo.xoom.http.RequestHeader;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.Version;
import io.vlingo.xoom.http.sample.user.NameData;

public class RequestHandler0Test extends RequestHandlerTestBase {

  @Rule
  @SuppressWarnings("deprecation")
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void simpleHandler() {
    final RequestHandler0 handler = new RequestHandler0(Method.GET, "/helloworld")
      .handle(() -> withSuccess(of(Created)));
    final Response response = handler.execute(Request.method(Method.GET), logger).outcome();

    assertNotNull(handler);
    assertEquals(Method.GET, handler.method);
    assertEquals("/helloworld", handler.path);
    assertResponsesAreEquals(of(Created), response);
  }

  @Test
  public void simpleHandlerWithBinaryResponse() {
    byte[] body = {1, 2, 1, 2};
    final RequestHandler0 handler = new RequestHandler0(Method.GET, "/helloworld")
      .handle(() -> withSuccess(Response.of(Created, Body.from(body, Body.Encoding.None))));
    final Response response = handler.execute(Request.method(Method.GET), logger).outcome();

    assertNotNull(handler);
    assertEquals(Method.GET, handler.method);
    assertEquals("/helloworld", handler.path);
    assertEquals(Body.from(body, Body.Encoding.None).binaryContent(), response.entity.binaryContent());
  }

  @Test()
  public void throwExceptionWhenNoHandlerIsDefined() {
    thrown.expect(HandlerMissingException.class);
    thrown.expectMessage("No handler defined for GET /helloworld");

    final RequestHandler0 handler = new RequestHandler0(Method.GET, "/helloworld");
    handler.execute(Request.method(Method.GET), logger);
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
    final Response response = handler.execute(request, mappedParameters, logger).outcome();

    assertNotNull(handler);
    assertEquals(Method.GET, handler.method);
    assertEquals("/helloworld", handler.path);
    assertResponsesAreEquals(of(Created), response);
  }

  //region adding handlers to RequestHandler0

  @Test
  public void addingHandlerParam() {
    final Request request = Request.has(Method.GET)
      .and(URI.create("/user/admin"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Collections.singletonList(
        new Action.MappedParameter("String", "admin"))
      );

    final RequestHandler1<String> handler = new RequestHandler0(Method.GET, "/user/{userId}")
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

    final RequestHandler1<NameData> handler = new RequestHandler0(Method.GET, "/user/admin/name")
      .body(NameData.class);

    assertResolvesAreEquals(body(NameData.class), handler.resolver);
    assertEquals(new NameData("John", "Doe"), handler.resolver.apply(request, mappedParameters));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void addingHandlerBodyWithMapper() {
    final Request request = Request.has(Method.POST)
      .and(URI.create("/user/admin/name"))
      .and(Body.from("{\"given\":\"John\",\"family\":\"Doe\"}"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.POST, "ignored", Collections.singletonList(
        new Action.MappedParameter("String", "admin"))
      );

    final RequestHandler1<NameData> handler1 = new RequestHandler0(Method.GET, "/user/admin/name")
      .body(NameData.class, TestMapper.class);

    assertResolvesAreEquals(body(NameData.class, new TestMapper()), handler1.resolver);
    assertEquals(new NameData("John", "Doe"), handler1.resolver.apply(request, mappedParameters));
  }


  @Test
  public void addingHandlerBodyWithMediaTypeMapper() {
    final Request request = Request.has(Method.POST)
      .and(URI.create("/user/admin/name"))
      .and(Body.from("{\"given\":\"John\",\"family\":\"Doe\"}"))
      .and(RequestHeader.of(RequestHeader.ContentType, "application/json"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.POST, "ignored", Collections.singletonList(
        new Action.MappedParameter("String", "admin"))
      );

    final RequestHandler1<NameData> handler1 = new RequestHandler0(Method.GET, "/user/admin/name")
      .body(NameData.class);

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

    final RequestHandler1<String> handler = new RequestHandler0(Method.GET, "/user")
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

    final RequestHandler1<Header> handler = new RequestHandler0(Method.GET, "/user")
      .header("Host");

    assertResolvesAreEquals(header("Host"), handler.resolver);
    assertEquals(hostHeader, handler.resolver.apply(request, mappedParameters));
  }

  //endregion
}
