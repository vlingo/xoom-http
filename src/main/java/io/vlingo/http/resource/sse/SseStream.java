// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.sse;

import java.util.Collection;

public interface SseStream {
  void publish(final SseEvent event);
  void publish(final Collection<SseEvent> events);
  void sendTo(final SseSubscriber subscriber, final SseEvent event);
  void sendTo(final SseSubscriber subscriber, final Collection<SseEvent> events);
  void subscribe(final SseSubscriber subscriber);
  void unsubscribe(final SseSubscriber subscriber);
}
