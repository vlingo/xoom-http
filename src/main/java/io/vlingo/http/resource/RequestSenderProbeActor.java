// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.nio.ByteBuffer;

import io.vlingo.actors.Actor;
import io.vlingo.common.Cancellable;
import io.vlingo.common.Scheduled;
import io.vlingo.http.Request;
import io.vlingo.http.resource.Client.Configuration;
import io.vlingo.wire.channel.ResponseChannelConsumer;
import io.vlingo.wire.fdx.bidirectional.ClientRequestResponseChannel;
import io.vlingo.wire.message.ByteBufferAllocator;
import io.vlingo.wire.message.Converters;

/**
 * Sends {@code Request} messages and probes for
 * incoming channel responses. This can be used
 * to prevent channel probe misses due to worker
 * stowing messages.
 */
public class RequestSenderProbeActor extends Actor implements RequestSender, Scheduled<Object> {
  private final ByteBuffer buffer;
  private final ClientRequestResponseChannel channel;
  private final Cancellable cancellable;
  //private final String testId;

  @SuppressWarnings("unchecked")
  public RequestSenderProbeActor(final Configuration configuration, final ResponseChannelConsumer consumer, final String testId) throws Exception {
    this.channel = ClientConsumerCommons.clientChannel(configuration, consumer, logger());
    this.cancellable = stage().scheduler().schedule(selfAs(Scheduled.class), null, 1, configuration.probeInterval);
    this.buffer = ByteBufferAllocator.allocate(configuration.writeBufferSize);
    //this.testId = testId;
  }

  @Override
  public void intervalSignal(final Scheduled<Object> scheduled, final Object data) {
    channel.probeChannel();
  }

  @Override
  public void sendRequest(final Request request) {
    buffer.clear();
    buffer.put(Converters.textToBytes(request.toString()));
    buffer.flip();
    channel.requestWith(buffer);
  }

  @Override
  public void stop() {
    cancellable.cancel();
    channel.close();

    super.stop();
  }
}
