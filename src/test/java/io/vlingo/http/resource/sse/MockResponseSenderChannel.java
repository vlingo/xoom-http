// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.sse;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseParser;
import io.vlingo.wire.channel.RequestResponseContext;
import io.vlingo.wire.channel.ResponseSenderChannel;
import io.vlingo.wire.message.ConsumerByteBuffer;

public class MockResponseSenderChannel implements ResponseSenderChannel {
  public AtomicInteger abandonCount = new AtomicInteger(0);
  public AtomicInteger respondWithCount = new AtomicInteger(0);
  public AtomicReference<Response> response = new AtomicReference<>();
  public TestUntil untilAbandon;
  public TestUntil untilRespondWith;

  @Override
  public void abandon(final RequestResponseContext<?> context) {
    abandonCount.incrementAndGet();
    if (untilAbandon != null) untilAbandon.happened();
  }

  @Override
  public void respondWith(final RequestResponseContext<?> context, final ConsumerByteBuffer buffer) {
    final ResponseParser parser = ResponseParser.parserFor(buffer.asByteBuffer());
    response.set(parser.fullResponse());
    respondWithCount.incrementAndGet();
    if (untilRespondWith != null) untilRespondWith.happened();
  }
}
