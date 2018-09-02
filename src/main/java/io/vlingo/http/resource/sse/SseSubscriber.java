// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.sse;

import io.vlingo.wire.channel.RequestResponseContext;
import io.vlingo.wire.message.ConsumerByteBuffer;

public class SseSubscriber {
  private final RequestResponseContext<?> context;
  private final String lastEventId;

  public SseSubscriber(final RequestResponseContext<?> context, final String lastEventId) {
    this.context = context;
    this.lastEventId = lastEventId;
  }

  public SseSubscriber(final RequestResponseContext<?> context) {
    this(context, "");
  }

  public void close() {
    context.abandon();
  }

  public String id() {
    return context.id();
  }

  public boolean hasLastEventId() {
    return lastEventId != null && !lastEventId.isEmpty();
  }

  public String lastEventId() {
    return lastEventId;
  }

  public void sendRawEvent(final ConsumerByteBuffer buffer) {
    context.respondWith(buffer);
  }
}
