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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.util.Collections;

import static io.vlingo.http.Response.Status.Created;
import static io.vlingo.http.Response.Status.Ok;
import static io.vlingo.http.resource.serialization.JsonSerialization.serialized;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RequestHandler0Test {
  private MockCompletesEventuallyResponse completes;

  @Before()
  public void setup() {
    completes = new MockCompletesEventuallyResponse();
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void simpleHandler() {
    final Response response = Response.of(Created);
    final RequestHandler0 handler = new RequestHandler0(Method.GET, "/helloworld")
      .handle((completes) -> completes.with(response));

    assertNotNull(handler);
    assertEquals(Method.GET, handler.method);
    assertEquals("/helloworld", handler.path);
    handler.execute(completes);
    assertEquals(response, completes.response);
  }

  @Test
  public void throwExceptionWhenNoHandlerIsDefined() {
    thrown.expect(HandlerMissingException.class);
    thrown.expectMessage("No handle defined for GET /helloworld");

    final RequestHandler0 handler = new RequestHandler0(Method.GET, "/helloworld");
    handler.execute(completes);
  }

  @Test
  public void actionSignatureIsEmpty() {
    final RequestHandler0 handler = new RequestHandler0(Method.GET, "/helloworld")
      .handle((completes) -> completes.with(Response.of(Created)));

    assertEquals("", handler.actionSignature);
  }

  @Test
  public void executeWithRequestAndMappedParametersHasToReturnTheSameAsExecute() {
    final Request request = Request.has(Method.GET)
      .and(URI.create("/hellworld"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "ignored", Collections.emptyList());
    final Response response = Response.of(Created);
    final RequestHandler0 handler = new RequestHandler0(Method.GET, "/helloworld")
      .handle((completes) -> completes.with(response));

    assertNotNull(handler);
    assertEquals(Method.GET, handler.method);
    assertEquals("/helloworld", handler.path);
    handler.execute(request, mappedParameters, completes);
    assertEquals(response, completes.response);
  }

  @Test
  public void executeWithBody() {
    final Request request = Request.has(Method.POST)
      .and(URI.create("/user/admin/name"))
      .and(Version.Http1_1)
      .and(Body.from("{\"given\":\"John\",\"family\":\"Doe\"}"));
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.POST, "ignored", Collections.emptyList());
    final Response response = Response.of(Ok, serialized(new NameData("John", "Doe")));
    final RequestHandler1<NameData> handler = new RequestHandler0(Method.GET, "/user/admin/name")
      .body(NameData.class)
      .handle((completes, nameData) -> completes.with(response));
    handler.execute(request, mappedParameters, completes);
    assertEquals(response, completes.response);
  }
}
