// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.common.Tuple2;
import io.vlingo.xoom.http.Response.Status;
import io.vlingo.xoom.http.resource.Actions;
import io.vlingo.xoom.http.resource.Configuration.Sizing;
import io.vlingo.xoom.http.resource.Configuration.Timing;
import io.vlingo.xoom.http.resource.ConfigurationResource;
import io.vlingo.xoom.http.resource.Resource;
import io.vlingo.xoom.http.resource.Resources;
import io.vlingo.xoom.http.resource.Server;
import io.vlingo.xoom.http.sample.user.ProfileResource;

public class FiltersTest {
  private static final Random random = new Random();
  private static AtomicInteger PORT_TO_USE = new AtomicInteger(1_000 + random.nextInt(45_000));

  private static String headerAcceptOriginAny = "*";
  private static String responseHeaderAcceptAllHeaders = "X-Requested-With, Content-Type, Content-Length";
  private static String responseHeaderAcceptMethodsAll = "POST,GET,PUT,PATCH,DELETE";

  private static String headerAcceptOriginHelloWorld = "hello.world";
  private static String responseHeaderAcceptHeadersHelloWorld = "X-Requested-With, Content-Type, Content-Length";
  private static String responseHeaderAcceptMethodsHelloWorld = "POST,GET";

  private static String headerAcceptOriginHelloCORS = "hello.cors";
  private static String responseHeaderAcceptHeadersHelloCORS = "Content-Type, Content-Length";
  private static String responseHeaderAcceptMethodsHelloCORS = "POST,GET,PUT";


  private int port;

  @Test
  public void testThatRequestFiltersProcess() throws Exception {
    RequestFilter1 filter1 = new RequestFilter1();
    RequestFilter1 filter2 = new RequestFilter1();
    RequestFilter1 filter3 = new RequestFilter1();

    final Filters filters = Filters.are(Arrays.asList(filter1, filter2, filter3), Filters.noResponseFilters());

    final Request request1 = Request.has(Method.GET).and(new URI("/"));

    Request request2 = request1;

    for (int times = 0; times < 5; ++times) {
      request2 = filters.process(request2);
    }

    assertEquals(request1, request2);

    assertEquals(5, filter1.count);
    assertEquals(4, filter2.count);
    assertEquals(4, filter3.count);

    filters.stop();

    assertTrue(filter1.stopped);
    assertTrue(filter2.stopped);
    assertTrue(filter3.stopped);
  }

  @Test
  public void testThatResponseFiltersProcess() throws Exception {
    ResponseFilter1 filter1 = new ResponseFilter1();
    ResponseFilter1 filter2 = new ResponseFilter1();
    ResponseFilter1 filter3 = new ResponseFilter1();

    final Filters filters = Filters.are(Filters.noRequestFilters(), Arrays.asList(filter1, filter2, filter3));

    final Response response1 = Response.of(Status.Ok);

    Response response2 = response1;

    for (int times = 0; times < 5; ++times) {
      response2 = filters.process(response2);
    }

    assertEquals(response1, response2);

    assertEquals(5, filter1.count);
    assertEquals(4, filter2.count);
    assertEquals(4, filter3.count);

    filters.stop();

    assertTrue(filter1.stopped);
    assertTrue(filter2.stopped);
    assertTrue(filter3.stopped);
  }

  @Test
  public void testThatCORSOriginAnyAllowed() {
    final CORSResponseFilter filter = new CORSResponseFilter();

    final List<ResponseHeader> headers =
            Arrays.asList(
                    ResponseHeader.of(ResponseHeader.AccessControlAllowOrigin, headerAcceptOriginAny),
                    ResponseHeader.of(ResponseHeader.AccessControlAllowHeaders, responseHeaderAcceptAllHeaders),
                    ResponseHeader.of(ResponseHeader.AccessControlAllowMethods, responseHeaderAcceptMethodsAll));

    filter.originHeadersFor(headerAcceptOriginAny, headers);

    //////////////// request: *

    final Request requestAny = Request.has(Method.GET).and(RequestHeader.of(RequestHeader.Origin, headerAcceptOriginAny));

    final Response responseAny = Response.of(Status.Ok).include(ResponseHeader.contentLength(0));

    final Tuple2<Response, Boolean> anyFilteredResponse = filter.filter(requestAny, responseAny);

    assertTrue(anyFilteredResponse._2);
    assertEquals(headerAcceptOriginAny, anyFilteredResponse._1.headerValueOr(ResponseHeader.AccessControlAllowOrigin, null));
    assertEquals(responseHeaderAcceptAllHeaders, anyFilteredResponse._1.headerValueOr(ResponseHeader.AccessControlAllowHeaders, null));
    assertEquals(responseHeaderAcceptMethodsAll, anyFilteredResponse._1.headerValueOr(ResponseHeader.AccessControlAllowMethods, null));

    //////////////// request: hello.world

    final Request requestHelloWorld = Request.has(Method.GET).and(RequestHeader.of(RequestHeader.Origin, headerAcceptOriginHelloWorld));

    final Response responseHelloWorld = Response.of(Status.Ok).include(ResponseHeader.contentLength(0));

    final Tuple2<Response, Boolean> helloWorldFilteredResponse = filter.filter(requestHelloWorld, responseHelloWorld);

    assertTrue(helloWorldFilteredResponse._2);
    assertEquals(headerAcceptOriginAny, helloWorldFilteredResponse._1.headerOf(ResponseHeader.AccessControlAllowOrigin).value);
    assertEquals(responseHeaderAcceptAllHeaders, helloWorldFilteredResponse._1.headerOf(ResponseHeader.AccessControlAllowHeaders).value);
    assertEquals(responseHeaderAcceptMethodsAll, helloWorldFilteredResponse._1.headerOf(ResponseHeader.AccessControlAllowMethods).value);
  }


  @Test
  public void testThatCORSOriginSomeAllowed() {
    final CORSResponseFilter filter = new CORSResponseFilter();

    final List<ResponseHeader> headersHelloWorld =
            Arrays.asList(
                    ResponseHeader.of(ResponseHeader.AccessControlAllowOrigin, headerAcceptOriginHelloWorld),
                    ResponseHeader.of(ResponseHeader.AccessControlAllowHeaders, responseHeaderAcceptHeadersHelloWorld),
                    ResponseHeader.of(ResponseHeader.AccessControlAllowMethods, responseHeaderAcceptMethodsHelloWorld));

    final List<ResponseHeader> headersHelloCORS =
            Arrays.asList(
                    ResponseHeader.of(ResponseHeader.AccessControlAllowOrigin, headerAcceptOriginHelloCORS),
                    ResponseHeader.of(ResponseHeader.AccessControlAllowHeaders, responseHeaderAcceptHeadersHelloCORS),
                    ResponseHeader.of(ResponseHeader.AccessControlAllowMethods, responseHeaderAcceptMethodsHelloCORS));

    filter.originHeadersFor(headerAcceptOriginHelloWorld, headersHelloWorld);
    filter.originHeadersFor(headerAcceptOriginHelloCORS, headersHelloCORS);

    //////////////// request: hello.world

    final Request requestHelloWorld = Request.has(Method.GET).and(RequestHeader.of(RequestHeader.Origin, headerAcceptOriginHelloWorld));

    final Response responseHelloWorld = Response.of(Status.Ok).include(ResponseHeader.contentLength(0));

    final Tuple2<Response, Boolean> helloWorldFilteredResponse = filter.filter(requestHelloWorld, responseHelloWorld);

    assertTrue(helloWorldFilteredResponse._2);
    assertEquals(headerAcceptOriginHelloWorld, helloWorldFilteredResponse._1.headerOf(ResponseHeader.AccessControlAllowOrigin).value);
    assertEquals(responseHeaderAcceptHeadersHelloWorld, helloWorldFilteredResponse._1.headerOf(ResponseHeader.AccessControlAllowHeaders).value);
    assertEquals(responseHeaderAcceptMethodsHelloWorld, helloWorldFilteredResponse._1.headerOf(ResponseHeader.AccessControlAllowMethods).value);

    //////////////// request: hello.cors

    final Request requestHelloCORS = Request.has(Method.GET).and(RequestHeader.of(RequestHeader.Origin, headerAcceptOriginHelloCORS));

    final Response responseHelloCORS = Response.of(Status.Ok).include(ResponseHeader.contentLength(0));

    final Tuple2<Response, Boolean> helloCORSFilteredResponse = filter.filter(requestHelloCORS, responseHelloCORS);

    assertTrue(helloCORSFilteredResponse._2);
    assertEquals(headerAcceptOriginHelloCORS, helloCORSFilteredResponse._1.headerOf(ResponseHeader.AccessControlAllowOrigin).value);
    assertEquals(responseHeaderAcceptHeadersHelloCORS, helloCORSFilteredResponse._1.headerOf(ResponseHeader.AccessControlAllowHeaders).value);
    assertEquals(responseHeaderAcceptMethodsHelloCORS, helloCORSFilteredResponse._1.headerOf(ResponseHeader.AccessControlAllowMethods).value);

    //////////////// request: *

    final Request requestAny = Request.has(Method.GET).and(RequestHeader.of(RequestHeader.Origin, headerAcceptOriginAny));

    final Response responseAny = Response.of(Status.Ok).include(ResponseHeader.contentLength(0));

    final Tuple2<Response, Boolean> anyFilteredResponse = filter.filter(requestAny, responseAny);

    assertTrue(anyFilteredResponse._2);
    assertNull(headerAcceptOriginAny, anyFilteredResponse._1.headerValueOr(ResponseHeader.AccessControlAllowOrigin, null));
    assertNull(responseHeaderAcceptAllHeaders, anyFilteredResponse._1.headerValueOr(ResponseHeader.AccessControlAllowHeaders, null));
    assertNull(responseHeaderAcceptMethodsAll, anyFilteredResponse._1.headerValueOr(ResponseHeader.AccessControlAllowMethods, null));
  }

  @Test
  public void testThatServerStartsWithFilters() {
    final World world = World.startWithDefaults("filters");

    Resource<?> resource =
            ConfigurationResource.defining("profile", ProfileResource.class, 5,
              Actions.canBe("PUT", "/users/{userId}/profile", "define(String userId, body:io.vlingo.xoom.http.sample.user.ProfileData profileData)", "io.vlingo.xoom.http.sample.user.ProfileDataMapper")
                      .also("GET", "/users/{userId}/profile", "query(String userId)", "io.vlingo.xoom.http.sample.user.ProfileDataMapper")
                      .thatsAll());

    port = PORT_TO_USE.incrementAndGet();

    Server server =
            Server.startWith(
                    world.stage(),
                    Resources.are(resource),
                    Filters.are(Arrays.asList(new RequestFilter1()), Filters.noResponseFilters()),
                    port,
                    Sizing.define(),
                    Timing.define());

    assertNotNull(server);
  }

  @Before
  public void setUp() {
  }

  private static class RequestFilter1 extends RequestFilter {
    int count;
    boolean stopped;

    RequestFilter1() {
      count = 0;
      stopped = false;
    }

    @Override
    public Tuple2<Request, Boolean> filter(final Request request) {
      return Tuple2.from(request, ++count < 5);
    }

    @Override
    public void stop() {
      stopped = true;
    }
  }

  private static class ResponseFilter1 extends ResponseFilter {
    int count;
    boolean stopped;

    ResponseFilter1() {
      count = 0;
      stopped = false;
    }

    @Override
    public Tuple2<Response, Boolean> filter(final Response response) {
      return Tuple2.from(response, ++count < 5);
    }

    @Override
    public void stop() {
      stopped = true;
    }
  }
}
