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
import io.vlingo.http.*;
import io.vlingo.http.resource.serialization.JsonSerialization;
import io.vlingo.http.sample.user.NameData;
import io.vlingo.http.sample.user.model.Name;
import io.vlingo.http.sample.user.model.User;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.util.Collections;

import static io.vlingo.common.Completes.withSuccess;
import static io.vlingo.http.Response.Status.*;
import static io.vlingo.http.Response.of;
import static io.vlingo.http.resource.ParameterResolver.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RequestHandler0Test extends RequestHandlerTestBase {

  @Rule
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
  public void objectMappedToMediaType() {
    // todo: test failure cases, which return a different result in the completion pipeline
    // todo: add media mimeType failure test
    // todo: create issue for failed actor will miss messages, need supervisor
    // todo: mocks discussion
    // todo: review extraction of execution code
    Name name = new Name("first", "last");
    final RequestHandler0 handler = new RequestHandler0(Method.GET, "/helloworld")
      // todo change signature to avoid casting
      .handle(() -> withSuccess(ObjectResponse.of(Ok, name, Name.class)))
      .mapper(defaultMediaTypeMapperForJson());

    final Response response = handler.execute(Request.method(Method.GET), logger).outcome();

    assertNotNull(handler);
    assertEquals(Method.GET, handler.method);
    assertEquals("/helloworld", handler.path);
    String nameAsJson = JsonSerialization.serialized(name);
    assertResponsesAreEquals(of(Ok,
      ResponseHeader.headers(ResponseHeader.ContentType, MediaType.Json().toString()), nameAsJson),
      response);
  }


  @Test
  public void objectNotMappedToMediaType() {
    Name name = new Name("first", "last");
    final RequestHandler0 handler = new RequestHandler0(Method.GET, "/helloworld")
      .handle(() -> withSuccess(ObjectResponse.of(Ok, name, Name.class)))
      .mapper(defaultMediaTypeMapperForJson());

    final Response response = handler.execute(
      Request.method(Method.GET).header(RequestHeader.Accept, "none/none"), logger).outcome();

    assertNotNull(handler);
    assertEquals(Method.GET, handler.method);
    assertEquals("/helloworld", handler.path);

    assertResponsesAreEquals(of(UnsupportedMediaType), response);
  }


  @Test
  public void errorHandlerInvoked() {
    final RequestHandler0 handler = new RequestHandler0(Method.GET, "/helloworld")
      .handle((RequestHandler0.Handler0)() -> {
        throw new RuntimeException("Test Handler exception");
      })
      .onError(
        (error) -> Completes.withSuccess(Response.of(Response.Status.Imateapot))
    );
    Completes<Response> responseCompletes = handler.execute(Request.method(Method.GET), logger);
    assertResponsesAreEquals(Response.of(Imateapot), responseCompletes.await());
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
      .body(NameData.class, defaultMediaTypeMapperForJson());

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
