// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource.sse;

import java.util.Collection;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.ActorInstantiator;

public interface SseFeed {
  void to(final Collection<SseSubscriber> subscribers);

  public static abstract class SseFeedInstantiator<A extends Actor> implements ActorInstantiator<A> {
    private static final long serialVersionUID = 1300345629189223793L;

    protected Class<A> feedClass;
    protected String streamName;
    protected int feedPayload;
    protected String feedDefaultId;

    public SseFeedInstantiator() { }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void set(final String name, final Object value) {
      switch (name) {
      case "feedClass":
        this.feedClass = (Class) value;
        break;
      case "streamName":
        this.streamName = (String) value;
        break;
      case "feedPayload":
        this.feedPayload = (int) value;
        break;
      case "feedDefaultId":
        this.feedDefaultId = (String) value;
        break;
      }
    }

    @Override
    public Class<A> type() {
      return feedClass;
    }
  }
}
