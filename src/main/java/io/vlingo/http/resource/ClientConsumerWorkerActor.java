// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import io.vlingo.actors.Actor;
import io.vlingo.actors.CompletesEventually;
import io.vlingo.common.Completes;
import io.vlingo.common.Scheduled;
import io.vlingo.http.Request;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseParser;
import io.vlingo.http.resource.Client.Configuration;
import io.vlingo.wire.channel.ResponseChannelConsumer;
import io.vlingo.wire.message.ByteBufferAllocator;
import io.vlingo.wire.message.ConsumerByteBuffer;
import io.vlingo.wire.message.Converters;

/**
 * Common behavior implemented by the worker fulfilling the {@code ClientConsumer} contract.
 */
public class ClientConsumerWorkerActor extends Actor implements ClientConsumer {
  private CompletesEventually completesEventually;
  private final State state;

  /**
   * Constructs my default state.
   * @param configuration the Configuration
   * @throws Exception when the ClientConsumer cannot be created
   */
  @SuppressWarnings("unchecked")
  public ClientConsumerWorkerActor(final Configuration configuration) throws Exception {
    this.state =
            new State(
                    configuration,
                    ClientConsumerCommons.clientChannel(configuration, selfAs(ResponseChannelConsumer.class), logger()),
                    null,
                    stage().scheduler().schedule(selfAs(Scheduled.class), null, 1, configuration.probeInterval),
                    ByteBufferAllocator.allocate(configuration.writeBufferSize));
  }

  /**
   * @see io.vlingo.wire.channel.ResponseChannelConsumer#consume(io.vlingo.wire.message.ConsumerByteBuffer)
   */
  @Override
  public void consume(final ConsumerByteBuffer buffer) {
    if (state.parser == null) {
      state.parser = ResponseParser.parserFor(buffer.asByteBuffer());
    } else {
      state.parser.parseNext(buffer.asByteBuffer());
    }
    buffer.release();

    // don't disperse stowed messages unless a full response has arrived
    if (state.parser.hasFullResponse()) {
      while (state.parser.hasFullResponse()) {
        final Response response = state.parser.fullResponse();
        completesEventually.with(response);
      }
      completesEventually = null;
      disperseStowedMessages();
    }
  }

  /**
   * @see io.vlingo.common.Scheduled#intervalSignal(io.vlingo.common.Scheduled, java.lang.Object)
   */
  @Override
  public void intervalSignal(final Scheduled<Object> scheduled, final Object data) {
    state.channel.probeChannel();
  }

  /**
   * @see io.vlingo.http.resource.ClientConsumer#requestWith(io.vlingo.http.Request, io.vlingo.common.Completes)
   */
  @Override
  public Completes<Response> requestWith(final Request request, final Completes<Response> completes) {
    completesEventually = stage().world().completesFor(completes);

    state.buffer.clear();
    state.buffer.put(Converters.textToBytes(request.toString()));
    state.buffer.flip();
    state.channel.requestWith(state.buffer);

    stowMessages(ResponseChannelConsumer.class);

    return completes;
  }

  /**
   * @see io.vlingo.actors.Stoppable#stop()
   */
  @Override
  public void stop() {
    state.channel.close();
    state.probe.cancel();
  }
}
