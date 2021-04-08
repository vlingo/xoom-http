// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import static io.vlingo.xoom.common.serialization.JsonSerialization.deserialized;
import static io.vlingo.xoom.common.serialization.JsonSerialization.serialized;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.http.Context;
import io.vlingo.xoom.http.Request;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.resource.FluentTestResource.Data;
import io.vlingo.xoom.wire.message.ByteBufferAllocator;
import io.vlingo.xoom.wire.message.Converters;

public class DynamicResourceDispatcherTest {
  private Dispatcher dispatcher;
  private FluentTestResource fluentResource;
  private Resources resources;
  private World world;

  private final ByteBuffer buffer = ByteBufferAllocator.allocate(512);

  private long previousResourceHandlerId = -1L;

  private final Data testData1 = Data.with("Test1", "The test description");

  private final String dataSerialized = serialized(testData1);

  private final String postDataMessage =
          "POST /res HTTP/1.1\nHost: vlingo.io\nContent-Length: " + dataSerialized.length() + "\n\n" + dataSerialized;

  // This test often fails on CI because there are a limited number of threads
  // for build/test processing. The responseData.resourceHandlerId holds a thread
  // id and is meant to change on each request/response. With only one thread this
  // test will absolutely fail because the request will always be handled by the
  // same thread. With only a few threads there is a high probability of failure,
  // because the same thread could easily be used to handle two back-to-back requests.
  // Actually even with any reasonable number of threads (8-12) it is still possible
  // to occasionally experience a failure. We feel safe to disable this test until
  // we identify a better way to safely guarantee that a different resource handler
  // will always handle back-to-back requests. We have successfully run this test
  // many times and also visually observed that the behavior does work correctly.
  @Ignore
  @Test
  public void testThatDispatchesThroughPool() {
    for (int count = 0; count < 3; ++count) {
      final Request request = Request.from(toByteBuffer(postDataMessage));
      final MockCompletesEventuallyResponse completes = new MockCompletesEventuallyResponse();

      final AccessSafely outcomes = completes.expectWithTimes(1);
      dispatcher.dispatchFor(new Context(request, completes));
      final int responseCount = outcomes.readFrom("completed");
      final Response response = outcomes.readFrom("response");

      assertEquals(1, responseCount);

      final Data responseData = deserialized(response.entity.content(), Data.class);

      assertEquals(testData1, responseData);

      System.out.println("previousResourceHandlerId=" + previousResourceHandlerId + " resourceHandlerId=" + responseData.resourceHandlerId);

      assertNotEquals(previousResourceHandlerId, responseData.resourceHandlerId);

      previousResourceHandlerId = responseData.resourceHandlerId;
    }
  }

  @Before
  public void setUp() throws Exception {
    world = World.start("test-dynamic-resource-dispatcher");

    fluentResource = new FluentTestResource(world);

    final Resource<?> resource = fluentResource.routes();

    resource.allocateHandlerPool(world.stage());

    resources = Resources.are(resource);

    dispatcher = new TestDispatcher(resources, world.defaultLogger());
  }

  @After
  public void tearDown() {
    world.terminate();
  }

  private ByteBuffer toByteBuffer(final String requestContent) {
    buffer.clear();
    buffer.put(Converters.textToBytes(requestContent));
    buffer.flip();
    return buffer;
  }
}
