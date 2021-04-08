// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource.sse;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.vlingo.xoom.actors.testkit.AccessSafely;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.ResponseParser;
import io.vlingo.xoom.wire.channel.RequestResponseContext;
import io.vlingo.xoom.wire.channel.ResponseSenderChannel;
import io.vlingo.xoom.wire.message.BasicConsumerByteBuffer;
import io.vlingo.xoom.wire.message.ConsumerByteBuffer;
import io.vlingo.xoom.wire.message.Converters;

public class MockResponseSenderChannel implements ResponseSenderChannel {
  public AtomicInteger abandonCount = new AtomicInteger(0);
  public AtomicReference<Response> eventsResponse = new AtomicReference<>();
  public AtomicInteger respondWithCount = new AtomicInteger(0);
  public AtomicReference<Response> response = new AtomicReference<>();
  private AccessSafely abandonSafely;
  private AccessSafely respondWithSafely;

  private boolean receivedStatus;

  public MockResponseSenderChannel() {
    respondWithSafely = expectRespondWith(0);
    abandonSafely = expectAbandon(0);
    receivedStatus = false;
  }

  @Override
  public void abandon(final RequestResponseContext<?> context) {
    final int count = abandonCount.incrementAndGet();
    abandonSafely.writeUsing("count", count);
  }

  @Override
  public void respondWith(final RequestResponseContext<?> context, final ConsumerByteBuffer buffer) {
    respondWith(context, buffer, false);
  }

  @Override
  public void respondWith(final RequestResponseContext<?> context, final ConsumerByteBuffer buffer, final boolean closeFollowing) {
    final ResponseParser parser = receivedStatus ?
            ResponseParser.parserForBodyOnly(buffer.asByteBuffer()) :
            ResponseParser.parserFor(buffer.asByteBuffer());

    if (!receivedStatus) {
      response.set(parser.fullResponse());
    } else {
      respondWithSafely.writeUsing("events", parser.fullResponse());
    }
    receivedStatus = true;
  }

  @Override
  public void respondWith(final RequestResponseContext<?> context, final Object response, final boolean closeFollowing) {
    final String textResponse = response.toString();

    final ConsumerByteBuffer buffer =
            new BasicConsumerByteBuffer(0, textResponse.length() + 1024)
            .put(Converters.textToBytes(textResponse)).flip();

    final ResponseParser parser = receivedStatus ?
            ResponseParser.parserForBodyOnly(buffer.asByteBuffer()) :
            ResponseParser.parserFor(buffer.asByteBuffer());

    if (!receivedStatus) {
      this.response.set(parser.fullResponse());
    } else {
      respondWithSafely.writeUsing("events", parser.fullResponse());
    }
    receivedStatus = true;
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
        .writingWith("events", (Response response) -> { respondWithCount.incrementAndGet(); eventsResponse.set(response); } )
        .readingWith("count", () -> respondWithCount.get())
        .readingWith("eventsResponse", () -> eventsResponse.get());
    return respondWithSafely;
  }
}
