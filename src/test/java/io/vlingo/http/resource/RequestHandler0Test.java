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
import io.vlingo.http.Request;
import io.vlingo.http.Response;
import io.vlingo.http.Version;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.util.Collections;

import static io.vlingo.http.Response.Status.Created;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RequestHandler0Test {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void simpleHandler() {
    final Response response = Response.of(Created);
    final RequestHandler0 handler = new RequestHandler0(Method.GET, "/helloworld")
      .handle(() -> response);

    assertNotNull(handler);
    assertEquals(Method.GET, handler.method());
    assertEquals("/helloworld", handler.path());
    assertEquals(response, handler.execute());
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
      .handle(() -> Response.of(Created));

    assertEquals("", handler.actionSignature());
  }

  @Test
  public void executeWithRequestAndMappedParametersHasToReturnTheSameAsExecute() {
    final Request request = Request.has(Method.GET)
      .and(URI.create("/hellworld"))
      .and(Version.Http1_1);
    final Action.MappedParameters mappedParameters =
      new Action.MappedParameters(1, Method.GET, "/helloworld", Collections.emptyList());
    final Response response = Response.of(Created);
    final RequestHandler0 handler = new RequestHandler0(Method.GET, "/helloworld")
      .handle(() -> response);

    assertNotNull(handler);
    assertEquals(Method.GET, handler.method());
    assertEquals("/helloworld", handler.path());
    assertEquals(response, handler.execute(request, mappedParameters));
  }
}
