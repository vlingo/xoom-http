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
  private AccessSafely abandonSafely = AccessSafely.afterCompleting(0);
  private AccessSafely respondWithSafely = AccessSafely.afterCompleting(0);

  @Override
  public void abandon(final RequestResponseContext<?> context) {
    final int count = abandonCount.incrementAndGet();
    abandonSafely.writeUsing("count", count);
  }

  @Override
  public void respondWith(final RequestResponseContext<?> context, final ConsumerByteBuffer buffer) {
    final ResponseParser parser = ResponseParser.parserFor(buffer.asByteBuffer());
    response.set(parser.fullResponse());
    final int count = respondWithCount.incrementAndGet();
    respondWithSafely.writeUsing("count", count);
  }

  /**
   * Answer with an AccessSafely which
   *  writes the abandon call count using "count" every time abandon(...) is called, and
   *  reads the abandon call count using "count".
   * @param n Number of times abandon must be called before readFrom will return.
   * @return
   */
  public AccessSafely expectAbandon(int n) {
    abandonSafely = AccessSafely.afterCompleting(n)
        .writingWith("count", (x) -> {})
        .readingWith("count", () -> abandonCount.get());
    return abandonSafely;
  }

  /**
   * Answer with an AccessSafely which
   *  writes the respondWith call count using "count" every time respondWith(...) is called, and
   *  reads the respondWith call count using "count".
   * @param n Number of times respondWith must be called before readFrom will return.
   * @return
   */
  public AccessSafely expectRespondWith(int n) {
    respondWithSafely = AccessSafely.afterCompleting(n)
        .writingWith("count", (x) -> {})
        .readingWith("count", () -> respondWithCount.get());
    return respondWithSafely;
  }

}
