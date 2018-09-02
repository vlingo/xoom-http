// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.sse;

public interface SseFeed {
  void startFor(final SseStream stream, final String streamName, final String clientId);
  void stopFor(final String streamName, final String clientId);
}
