// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import io.vlingo.xoom.actors.ActorInstantiator;
import io.vlingo.xoom.actors.Stoppable;
import io.vlingo.xoom.http.Request;
import io.vlingo.xoom.http.resource.Client.Configuration;
import io.vlingo.xoom.wire.channel.ResponseChannelConsumer;

/**
 * Sends {@code Request} messages in behalf of a client.
 */
public interface RequestSender extends Stoppable {
  /**
   * Sends the {@code request}.
   * @param request the Request to send
   */
  void sendRequest(final Request request);

  static class RequestSenderProbeInstantiator implements ActorInstantiator<RequestSenderProbeActor> {
    private static final long serialVersionUID = 2873944246822995889L;

    final Configuration configuration;
    final ResponseChannelConsumer consumer;
    final String testId;

    public RequestSenderProbeInstantiator(final Configuration configuration, final ResponseChannelConsumer consumer, final String testId) {
      this.configuration = configuration;
      this.consumer = consumer;
      this.testId = testId;
    }

    @Override
    public RequestSenderProbeActor instantiate() {
      try {
        return new RequestSenderProbeActor(configuration, consumer, testId);
      } catch (Exception e) {
        throw new IllegalArgumentException("Failed to instantiate " + type() + " because: " + e.getMessage(), e);
      }
    }

    @Override
    public Class<RequestSenderProbeActor> type() {
      return RequestSenderProbeActor.class;
    }
  }
}
