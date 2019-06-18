// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.sse;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseParser;
import io.vlingo.wire.channel.RequestResponseContext;
import io.vlingo.wire.channel.ResponseSenderChannel;
import io.vlingo.wire.message.ConsumerByteBuffer;

public class MockResponseSenderChannel implements ResponseSenderChannel {
  public AtomicInteger abandonCount = new AtomicInteger(0);
  public AtomicInteger respondWithCount = new AtomicInteger(0);
  public AtomicReference<Response> response = new AtomicReference<>();
  private AccessSafely abandonSafely;
  private AccessSafely respondWithSafely;

  @Override
  public void abandon(final RequestResponseContext<?> context) {
    abandonCount.incrementAndGet();
    if (abandonSafely != null) {
      abandonSafely.writeUsing("foo", "bar");
    }
  }

  @Override
  public void respondWith(final RequestResponseContext<?> context, final ConsumerByteBuffer buffer) {
    final ResponseParser parser = ResponseParser.parserFor(buffer.asByteBuffer());
    response.set(parser.fullResponse());
    respondWithCount.incrementAndGet();
    if (respondWithSafely != null) {
      respondWithSafely.writeUsing("foo", "bar");
    }
  }

  public void expectAbandon(int n) {
    if (abandonSafely != null) {
      throw new IllegalStateException("call to untilAbandon without corresponding call to completes");
    }
    abandonSafely = AccessSafely.afterCompleting(n).writingWith("foo", (x) -> {}).readingWith("foo", () -> "bar");
  }
  

  public void expectRespondWith(int n) {
    if (respondWithSafely != null) {
      throw new IllegalStateException("call to untilRespondWithTimes without corresponding call to completes");
    }
    respondWithSafely = AccessSafely.afterCompleting(n).writingWith("foo", (x) -> {}).readingWith("foo", () -> "bar");
  }
  
  public void untilCompletes() {
    if (abandonSafely != null) {
      abandonSafely.readFrom("foo");
      abandonSafely = null;
    }
    if (respondWithSafely != null) {
      respondWithSafely.readFrom("foo");
      respondWithSafely = null;
    }
  }

}
