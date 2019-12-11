// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.sse;

import java.util.Collection;

import io.vlingo.actors.Actor;
import io.vlingo.actors.ActorInstantiator;

public interface SseFeed {
  void to(final Collection<SseSubscriber> subscribers);

  static abstract class SseFeedInstantiator<A extends Actor> implements ActorInstantiator<A> {
    protected final Class<A> type;
    protected final String streamName;
    protected final int feedPayload;
    protected final String feedDefaultId;

    public SseFeedInstantiator(
            final Class<A> type,
            final String streamName,
            final int feedPayload,
            final String feedDefaultId) {
      this.type = type;
      this.streamName = streamName;
      this.feedPayload = feedPayload;
      this.feedDefaultId = feedDefaultId;
    }

    @Override
    public Class<A> type() {
      return type;
    }
  }
}
