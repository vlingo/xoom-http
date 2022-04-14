// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.resource.TestResponseChannelConsumer.Progress;
import io.vlingo.xoom.wire.channel.ResponseChannelConsumer;
import io.vlingo.xoom.wire.fdx.bidirectional.ClientRequestResponseChannel;
import io.vlingo.xoom.wire.fdx.bidirectional.netty.client.NettyClientRequestResponseChannel;
import io.vlingo.xoom.wire.message.ByteBufferAllocator;
import io.vlingo.xoom.wire.message.Converters;
import io.vlingo.xoom.wire.node.Address;
import io.vlingo.xoom.wire.node.AddressType;
import io.vlingo.xoom.wire.node.Host;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SinglePageApplicationResourceTest {

  private static final AtomicInteger baseServerPort = new AtomicInteger(19001);

  private final String contentRoot = "/content";
  private final String contextPath = "/app";

  private final ByteBuffer buffer = ByteBufferAllocator.allocate(65535);

  private Server server;
  private World world;

  private Progress progress;
  private ClientRequestResponseChannel client;

  @Test
  public void rootDefaultStaticFile() throws IOException {
    final String resource = "/index.html";
    final String content = readTextFile(contentRoot + resource);
    final String request = getRequest(contextPath + "/");
    final AccessSafely consumeCalls = progress.expectConsumeTimes(1);

    client.requestWith(toByteBuffer(request));

    while (consumeCalls.totalWrites() < 1) {
      client.probeChannel();
    }
    consumeCalls.readFrom("completed");

    final Response contentResponse = progress.responses.poll();

    assertEquals(1, progress.consumeCount.get());
    assertEquals(Response.Status.Ok, contentResponse.status);
    assertEquals(content, contentResponse.entity.content());
  }

  @Test
  public void rootStaticFile() throws IOException {
    final String resource = "/index.html";
    final String content = readTextFile(contentRoot + resource);
    final String request = getRequest(contextPath + resource);
    final AccessSafely consumeCalls = progress.expectConsumeTimes(1);

    client.requestWith(toByteBuffer(request));

    while (consumeCalls.totalWrites() < 1) {
      client.probeChannel();
    }
    consumeCalls.readFrom("completed");

    final Response contentResponse = progress.responses.poll();

    assertEquals(1, progress.consumeCount.get());
    assertEquals(Response.Status.Ok, contentResponse.status);
    assertEquals(content, contentResponse.entity.content());
  }

  @Test
  public void dynamicPath() throws IOException {
    final String content = readTextFile(contentRoot + "/index.html");
    final String request = getRequest(contextPath + "/customers");
    final AccessSafely consumeCalls = progress.expectConsumeTimes(1);

    client.requestWith(toByteBuffer(request));

    while (consumeCalls.totalWrites() < 1) {
      client.probeChannel();
    }
    consumeCalls.readFrom("completed");

    final Response contentResponse = progress.responses.poll();

    assertEquals(1, progress.consumeCount.get());
    assertEquals(Response.Status.Ok, contentResponse.status);
    assertEquals(content, contentResponse.entity.content());
  }

  @Test
  public void dynamicPathWithSubPath() throws IOException {
    final String content = readTextFile(contentRoot + "/index.html");
    final String request = getRequest(contextPath + "/customers/1");
    final AccessSafely consumeCalls = progress.expectConsumeTimes(1);

    client.requestWith(toByteBuffer(request));

    while (consumeCalls.totalWrites() < 1) {
      client.probeChannel();
    }
    consumeCalls.readFrom("completed");

    final Response contentResponse = progress.responses.poll();

    assertEquals(1, progress.consumeCount.get());
    assertEquals(Response.Status.Ok, contentResponse.status);
    assertEquals(content, contentResponse.entity.content());
  }

  @Test
  public void dynamicPathWithSubPathWithUUID() throws IOException {
    final String content = readTextFile(contentRoot + "/index.html");
    final String request = getRequest(contextPath + "/specialists/ea9124b6-2b34-4906-bcec-0a2fed9625b6/specializeIn");
    final AccessSafely consumeCalls = progress.expectConsumeTimes(1);

    client.requestWith(toByteBuffer(request));

    while (consumeCalls.totalWrites() < 1) {
      client.probeChannel();
    }
    consumeCalls.readFrom("completed");

    final Response contentResponse = progress.responses.poll();

    assertEquals(1, progress.consumeCount.get());
    assertEquals(Response.Status.Ok, contentResponse.status);
    assertEquals(content, contentResponse.entity.content());
  }

  @Test
  public void cssSubDirectoryStaticFile() throws IOException {
    final String resource = "/css/styles.css";
    final String content = readTextFile(contentRoot + resource);
    final String request = getRequest(contextPath + resource);
    final AccessSafely consumeCalls = progress.expectConsumeTimes(1);

    client.requestWith(toByteBuffer(request));

    while (consumeCalls.totalWrites() < 1) {
      client.probeChannel();
    }
    consumeCalls.readFrom("completed");

    final Response contentResponse = progress.responses.poll();

    assertEquals(1, progress.consumeCount.get());
    assertEquals(Response.Status.Ok, contentResponse.status);
    assertEquals(content, contentResponse.entity.content());
  }

  @Test
  public void jsSubDirectoryStaticFile() throws IOException {
    final String resource = "/js/vuetify.js";
    final String content = readTextFile(contentRoot + resource);
    final String request = getRequest(contextPath + resource);
    final AccessSafely consumeCalls = progress.expectConsumeTimes(1);

    client.requestWith(toByteBuffer(request));

    while (consumeCalls.totalWrites() < 1) {
      client.probeChannel();
    }
    consumeCalls.readFrom("completed");

    final Response contentResponse = progress.responses.poll();

    assertEquals(1, progress.consumeCount.get());
    assertEquals(Response.Status.Ok, contentResponse.status);
    assertEquals(content, contentResponse.entity.content());
  }

  @Test
  public void viewsSubDirectoryStaticFile() throws IOException {
    final String resource = "/views/About.vue";
    final String content = readTextFile(contentRoot + resource);
    final String request = getRequest(contextPath + resource);
    final AccessSafely consumeCalls = progress.expectConsumeTimes(1);

    client.requestWith(toByteBuffer(request));

    while (consumeCalls.totalWrites() < 1) {
      client.probeChannel();
    }
    consumeCalls.readFrom("completed");

    final Response contentResponse = progress.responses.poll();

    assertEquals(1, progress.consumeCount.get());
    assertEquals(Response.Status.Ok, contentResponse.status);
    assertEquals(content, contentResponse.entity.content());
  }

  @Before
  public void setUp() throws Exception {
    world = World.startWithDefaults("static-file-resources");

    int serverPort = baseServerPort.getAndIncrement();

    Resources resources = Resources.are(new SinglePageApplicationResource(contentRoot, contextPath).routes());
    Configuration.Sizing sizing = Configuration.Sizing.defineWith(2, 10, 100, 65535);
    Configuration.Timing timing = Configuration.Timing.defineWith(2, 2, 100);
    server = Server.startWith(world.stage(), resources, serverPort, sizing, timing);
    assertTrue(server.startUp().await(500L));

    progress = new Progress();
    ResponseChannelConsumer consumer = world.actorFor(ResponseChannelConsumer.class, Definition.has(TestResponseChannelConsumer.class, Definition.parameters(progress)));
    client = new NettyClientRequestResponseChannel(Address.from(Host.of("localhost"), serverPort, AddressType.NONE), consumer, 100, 10240);
  }


  @After
  public void tearDown() throws Exception {
    this.client.close();
    this.server.shutDown();
    Thread.sleep(200);
    this.world.terminate();
  }

  private String getRequest(final String filePath) {
    return "GET " + String.join("%20", filePath.split(" ")) + " HTTP/1.1\nHost: vlingo.io\n\n";
  }

  private byte[] readFile(final String path) throws IOException {
    final InputStream contentStream = StaticFilesResource.class.getResourceAsStream(path);
    if (contentStream != null && contentStream.available() > 0) {
      return IOUtils.toByteArray(contentStream);
    }
    throw new IllegalArgumentException("File not found.");
  }

  private String readTextFile(final String path) throws IOException {
    return new String(readFile(path), StandardCharsets.UTF_8);
  }

  private ByteBuffer toByteBuffer(final String requestContent) {
    buffer.clear();
    buffer.put(Converters.textToBytes(requestContent));
    buffer.flip();
    return buffer;
  }
}
