// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.Definition;
import io.vlingo.actors.World;
import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.http.Response;
import io.vlingo.http.resource.TestResponseChannelConsumer.Progress;
import io.vlingo.wire.channel.ResponseChannelConsumer;
import io.vlingo.wire.fdx.bidirectional.ClientRequestResponseChannel;
import io.vlingo.wire.message.ByteBufferAllocator;
import io.vlingo.wire.message.Converters;
import io.vlingo.wire.node.Address;
import io.vlingo.wire.node.AddressType;
import io.vlingo.wire.node.Host;

public class StaticFilesResourceTest {
  private static final AtomicInteger baseServerPort = new AtomicInteger(19001);

  private final ByteBuffer buffer = ByteBufferAllocator.allocate(65535);
  private ClientRequestResponseChannel client;
  private ResponseChannelConsumer consumer;
  private String contentRoot;
  private Progress progress;
  private java.util.Properties properties;
  private Server server;
  private int serverPort;
  private World world;

  @Test
  public void testThatServesRootStaticFile() throws IOException {
    final String resource = "/index.html";
    final String content = readTextFile(contentRoot + resource);
    final String request = getRequest(resource);
    client.requestWith(toByteBuffer(request));

    progress.untilConsumed = TestUntil.happenings(1);
    while (progress.untilConsumed.remaining() > 0) {
      client.probeChannel();
    }
    progress.untilConsumed.completes();

    final Response contentResponse = progress.responses.poll();

    assertEquals(1, progress.consumeCount.get());
    assertEquals(Response.Status.Ok, contentResponse.status);
    assertEquals(content, contentResponse.entity.content);
  }

  @Test
  public void testThatServesCssSubDirectoryStaticFile() throws IOException {
    final String resource = "/css/styles.css";
    final String content = readTextFile(contentRoot + resource);
    final String request = getRequest(resource);
    client.requestWith(toByteBuffer(request));

    progress.untilConsumed = TestUntil.happenings(1);
    while (progress.untilConsumed.remaining() > 0) {
      client.probeChannel();
    }
    progress.untilConsumed.completes();

    final Response contentResponse = progress.responses.poll();

    assertEquals(1, progress.consumeCount.get());
    assertEquals(Response.Status.Ok, contentResponse.status);
    assertEquals(content, contentResponse.entity.content);
  }

  @Test
  public void testThatServesJsSubDirectoryStaticFile() throws IOException {
    final String resource = "/js/vuetify.js";
    final String content = readTextFile(contentRoot + resource);
    final String request = getRequest(resource);
    client.requestWith(toByteBuffer(request));

    progress.untilConsumed = TestUntil.happenings(1);
    while (progress.untilConsumed.remaining() > 0) {
      client.probeChannel();
    }
    progress.untilConsumed.completes();

    final Response contentResponse = progress.responses.poll();

    assertEquals(1, progress.consumeCount.get());
    assertEquals(Response.Status.Ok, contentResponse.status);
    assertEquals(content, contentResponse.entity.content);
  }

  @Test
  public void testThatServesViewsSubDirectoryStaticFile() throws IOException {
    final String resource = "/views/About.vue";
    final String content = readTextFile(contentRoot + resource);
    final String request = getRequest(resource);
    client.requestWith(toByteBuffer(request));

    progress.untilConsumed = TestUntil.happenings(1);
    while (progress.untilConsumed.remaining() > 0) {
      client.probeChannel();
    }
    progress.untilConsumed.completes();

    final Response contentResponse = progress.responses.poll();

    assertEquals(1, progress.consumeCount.get());
    assertEquals(Response.Status.Ok, contentResponse.status);
    assertEquals(content, contentResponse.entity.content);
  }

  @Before
  public void setUp() throws Exception {

    world = World.startWithDefaults("static-file-resources");

    serverPort = baseServerPort.getAndIncrement();

    properties = new java.util.Properties();
    properties.setProperty("server.http.port", ""+serverPort);
    properties.setProperty("server.dispatcher.pool", "10");
    properties.setProperty("server.buffer.pool.size", "100");
    properties.setProperty("server.message.buffer.size", "65535");
    properties.setProperty("server.probe.interval", "2");
    properties.setProperty("server.probe.timeout", "2");
    properties.setProperty("server.processor.pool.size", "10");
    properties.setProperty("server.request.missing.content.timeout", "100");

    properties.setProperty("static.files.resource.pool", "5");
    contentRoot = System.getProperty("user.dir") + "/src/test/resources/content";
    properties.setProperty("static.files.resource.root", contentRoot);
    properties.setProperty("static.files.resource.subpaths", "[/, /css, /js, /views]");

    properties.setProperty("sse.stream.name.all", "/eventstreams/all");
    properties.setProperty("sse.stream.all.feed.class", "io.vlingo.http.sample.user.AllSseFeedActor");
    properties.setProperty("sse.stream.all.feed.payload", "50");
    properties.setProperty("sse.stream.all.feed.interval", "1000");
    properties.setProperty("sse.stream.all.feed.default.id", "-1");
    properties.setProperty("sse.stream.all.pool", "10");

    properties.setProperty("resource.name.profile", "[define, query]");

    properties.setProperty("resource.profile.handler", "io.vlingo.http.sample.user.ProfileResource");
    properties.setProperty("resource.profile.pool", "5");
    properties.setProperty("resource.profile.disallowPathParametersWithSlash", "false");

    properties.setProperty("action.profile.define.method", "PUT");
    properties.setProperty("action.profile.define.uri", "/users/{userId}/profile");
    properties.setProperty("action.profile.define.to", "define(String userId, body:io.vlingo.http.sample.user.ProfileData profileData)");
    properties.setProperty("action.profile.define.mapper", "io.vlingo.http.sample.user.ProfileDataMapper");

    properties.setProperty("action.profile.query.method", "GET");
    properties.setProperty("action.profile.query.uri", "/users/{userId}/profile");
    properties.setProperty("action.profile.query.to", "query(String userId)");
    properties.setProperty("action.profile.query.mapper", "io.vlingo.http.sample.user.ProfileDataMapper");

    server = Server.startWith(world.stage(), properties);
    assertTrue(server.startUp().await(500L));

    progress = new Progress();
    consumer = world.actorFor(ResponseChannelConsumer.class, Definition.has(TestResponseChannelConsumer.class, Definition.parameters(progress)));
    client = new ClientRequestResponseChannel(Address.from(Host.of("localhost"), serverPort, AddressType.NONE), consumer, 100, 10240, world.defaultLogger());
  }

  private String getRequest(final String filePath) {
    return "GET " + filePath + " HTTP/1.1\nHost: vlingo.io\n\n";
  }

  private byte[] readFile(final String path) throws IOException {
    final File file = new File(path);
    if (file.exists()) {
      return Files.readAllBytes(file.toPath());
    }
    throw new IllegalArgumentException("File not found.");
  }

  private String readTextFile(final String path) throws IOException {
    return new String(readFile(path), Charset.forName("UTF-8"));
  }

  private ByteBuffer toByteBuffer(final String requestContent) {
    buffer.clear();
    buffer.put(Converters.textToBytes(requestContent));
    buffer.flip();
    return buffer;
  }
}
