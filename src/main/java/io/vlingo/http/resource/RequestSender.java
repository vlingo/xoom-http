// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import io.vlingo.actors.Stoppable;
import io.vlingo.http.Request;

/**
 * Sends {@code Request} messages in behalf of a client.
 */
public interface RequestSender extends Stoppable {
  /**
   * Sends the {@code request}.
   * @param request the Request to send
   */
  void sendRequest(final Request request);
}
