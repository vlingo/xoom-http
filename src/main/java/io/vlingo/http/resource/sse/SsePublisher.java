// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.sse;

import io.vlingo.actors.Stoppable;

public interface SsePublisher extends Stoppable {
  void subscribe(final SseSubscriber subscriber);
  void unsubscribe(final SseSubscriber subscriber);
}
