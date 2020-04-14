// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.World;
import io.vlingo.common.Tuple2;
import io.vlingo.http.Response.Status;
import io.vlingo.http.resource.Actions;
import io.vlingo.http.resource.Configuration.Sizing;
import io.vlingo.http.resource.Configuration.Timing;
import io.vlingo.http.resource.ConfigurationResource;
import io.vlingo.http.resource.Resource;
import io.vlingo.http.resource.Resources;
import io.vlingo.http.resource.Server;
import io.vlingo.http.sample.user.ProfileResource;

public class FiltersTest {
  private static final Random random = new Random();
  private static AtomicInteger PORT_TO_USE = new AtomicInteger(1_000 + random.nextInt(45_000));

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
  public void testThatServerStartsWithFilters() {
    final World world = World.startWithDefaults("filters");

    Resource<?> resource =
            ConfigurationResource.defining("profile", ProfileResource.class, 5,
              Actions.canBe("PUT", "/users/{userId}/profile", "define(String userId, body:io.vlingo.http.sample.user.ProfileData profileData)", "io.vlingo.http.sample.user.ProfileDataMapper")
                      .also("GET", "/users/{userId}/profile", "query(String userId)", "io.vlingo.http.sample.user.ProfileDataMapper")
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
