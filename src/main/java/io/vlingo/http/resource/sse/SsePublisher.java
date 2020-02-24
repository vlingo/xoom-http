// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.sse;

import io.vlingo.actors.Actor;
import io.vlingo.actors.ActorInstantiator;
import io.vlingo.actors.Stoppable;
import io.vlingo.http.resource.sse.SseStreamResource.SsePublisherActor;

public interface SsePublisher extends Stoppable {
  void subscribe(final SseSubscriber subscriber);
  void unsubscribe(final SseSubscriber subscriber);

  static class SsePublisherInstantiator implements ActorInstantiator<SsePublisherActor> {
    private static final long serialVersionUID = -3527194754132755789L;

    private final String streamName;
    private final Class<? extends Actor> feedClass;
    private final int feedPayload;
    private final int feedInterval;
    private final String feedDefaultId;

    public SsePublisherInstantiator(final String streamName, final Class<? extends Actor> feedClass, final int feedPayload, final int feedInterval, final String feedDefaultId) {
      this.streamName = streamName;
      this.feedClass = feedClass;
      this.feedPayload = feedPayload;
      this.feedInterval = feedInterval;
      this.feedDefaultId = feedDefaultId;
    }

    @Override
    public SsePublisherActor instantiate() {
      return new SsePublisherActor(streamName, feedClass, feedPayload, feedInterval, feedDefaultId);
    }

    @Override
    public Class<SsePublisherActor> type() {
      return SsePublisherActor.class;
    }
  }
}
