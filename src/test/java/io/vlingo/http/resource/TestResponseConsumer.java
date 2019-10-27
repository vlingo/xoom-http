// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.http.Response;

public class TestResponseConsumer {
  public final AtomicReference<Response> responseHolder = new AtomicReference<>();
  public final AtomicInteger responseCount = new AtomicInteger(0);
  public final AtomicInteger unknownResponseCount = new AtomicInteger(0);
  private AccessSafely access = afterCompleting(0);
  private final Map<String,Integer> clientCounts = new HashMap<>();

  public TestResponseConsumer() { }

  public AccessSafely afterCompleting(final int happenings) {
    access = AccessSafely.afterCompleting(happenings);

    access.writingWith("response", (Response response) -> {
      final String testId = response.headerValueOr(Client.ClientIdCustomHeader, "");

      System.out.println("ID: " + testId);

      if (testId.isEmpty()) {
        System.out.println("Expected header missing: " + Client.ClientIdCustomHeader);
        //throw new IllegalStateException("Expected header missing: " + Client.ClientIdCustomHeader);
      }

      final Integer existingCount = clientCounts.getOrDefault(testId, 0);

      responseHolder.set(response);

      clientCounts.put(testId, existingCount + 1);

      responseCount.incrementAndGet();
    });
    access.readingWith("response", () -> responseHolder.get());
    access.readingWith("responseCount", () -> responseCount.get());
    access.readingWith("responseClientCounts", () -> clientCounts);

    access.writingWith("unknownResponseCount", (Integer increment) -> unknownResponseCount.incrementAndGet());
    access.readingWith("unknownResponseCount", () -> unknownResponseCount.get());

    access.readingWith("totalAllResponseCount", () -> responseCount.get() + unknownResponseCount.get());

    return access;
  }

  public static class KnownResponseConsumer implements ResponseConsumer {
    private final AccessSafely access;

    public KnownResponseConsumer(final AccessSafely access) {
      this.access = access;
    }

    @Override
    public void consume(final Response response) {
      System.out.println("KNOWN RESPONSE:\n" + response);
      access.writeUsing("response", response);
    }
  }

  public static class UnknownResponseConsumer implements ResponseConsumer {
    private final AccessSafely access;

    public UnknownResponseConsumer(final AccessSafely access) {
      this.access = access;
    }

    @Override
    public void consume(final Response response) {
      System.out.println("UNKNOWN RESPONSE:\n" + response);
      access.writeUsing("unknownResponseCount", 1);
    }
  }
}
