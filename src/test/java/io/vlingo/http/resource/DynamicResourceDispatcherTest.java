// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import static io.vlingo.common.serialization.JsonSerialization.deserialized;
import static io.vlingo.common.serialization.JsonSerialization.serialized;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.World;
import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.http.Context;
import io.vlingo.http.Request;
import io.vlingo.http.Response;
import io.vlingo.http.resource.FluentTestResource.Data;
import io.vlingo.wire.message.ByteBufferAllocator;
import io.vlingo.wire.message.Converters;

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
