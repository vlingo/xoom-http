// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.CompletesEventually;
import io.vlingo.xoom.actors.Returns;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.common.Scheduled;
import io.vlingo.xoom.http.Request;
import io.vlingo.xoom.http.RequestHeader;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.ResponseHeader;
import io.vlingo.xoom.http.ResponseParser;
import io.vlingo.xoom.http.resource.Client.Configuration;
import io.vlingo.xoom.wire.channel.ResponseChannelConsumer;
import io.vlingo.xoom.wire.message.ByteBufferAllocator;
import io.vlingo.xoom.wire.message.ConsumerByteBuffer;
import io.vlingo.xoom.wire.message.Converters;

/**
 * The client requester-consumer that handles request-responses using {@code X-Correlation-ID},
 * which enables it to request and consumer responses out of order and without expectation.
 */
public class ClientCorrelatingRequesterConsumerActor extends Actor implements ClientConsumer {
  private final Map<String, CompletesEventually> completables;
  private final State state;

  @SuppressWarnings("unchecked")
  public ClientCorrelatingRequesterConsumerActor(final Configuration configuration) throws Exception {
    this.state =
            new State(
                    configuration,
                    ClientConsumerCommons.clientChannel(configuration, selfAs(ResponseChannelConsumer.class), logger()),
                    null,
                    stage().scheduler().schedule(selfAs(Scheduled.class), null, 1, configuration.probeInterval),
                    ByteBufferAllocator.allocate(configuration.writeBufferSize));

    this.completables = new HashMap<>();
  }

  /**
   * @see io.vlingo.xoom.wire.channel.ResponseChannelConsumer#consume(io.vlingo.xoom.wire.message.ConsumerByteBuffer)
   */
  @Override
  public void consume(final ConsumerByteBuffer buffer) {
    try {
      if (state.parser == null) {
        state.parser = ResponseParser.parserFor(buffer.asByteBuffer());
      } else {
        state.parser.parseNext(buffer.asByteBuffer());
      }

      while (state.parser.hasFullResponse()) {
        final Response response = state.parser.fullResponse();
        final ResponseHeader correlationId = response.headers.headerOfOrDefault(ResponseHeader.XCorrelationID, state.correlationId);
        if (correlationId == null) {
          logger().warn("Client Consumer: Cannot complete response because no correlation id.");
          state.configuration.consumerOfUnknownResponses.consume(response);
        } else {
          if (state.parser.isKeepAliveConnection() && state.parser.isStreamContentType()) {
            state.correlationId = correlationId;
          }
          final CompletesEventually completes = state.configuration.keepAlive ?
                  completables.get(correlationId.value) :
                  completables.remove(correlationId.value);
          if (completes == null) {
            state.configuration.stage.world().defaultLogger().warn(
                    "Client Consumer: Cannot complete response because mismatched correlation id: " +
                     correlationId.value);
            state.configuration.consumerOfUnknownResponses.consume(response);
          } else {
            completes.with(response);
          }
        }
      }
    } finally {
      buffer.release();
    }
  }

  /**
   * @see io.vlingo.xoom.common.Scheduled#intervalSignal(io.vlingo.xoom.common.Scheduled, java.lang.Object)
   */
  @Override
  public void intervalSignal(final Scheduled<Object> scheduled, final Object data) {
    state.channel.probeChannel();
  }

  /**
   * @see io.vlingo.xoom.http.resource.ClientConsumer#requestWith(io.vlingo.xoom.http.Request, io.vlingo.xoom.common.Completes)
   */
  @Override
  public Completes<Response> requestWith(final Request request, final Completes<Response> completes) {
    RequestHeader correlationId = request.headers.headerOf(RequestHeader.XCorrelationID);

    final Request readyRequest;

    if (correlationId == null) {
      correlationId = RequestHeader.of(RequestHeader.XCorrelationID, UUID.randomUUID().toString());
      readyRequest = request.and(correlationId);
    } else {
      readyRequest = request;
    }

    completables.put(correlationId.value, stage().world().completesFor(Returns.value(completes)));

    state.buffer.clear();
    state.buffer.put(Converters.textToBytes(readyRequest.toString()));
    state.buffer.flip();
    state.channel.requestWith(state.buffer);

    return completes;
  }

  /**
   * @see io.vlingo.xoom.actors.Stoppable#stop()
   */
  @Override
  public void stop() {
    state.channel.close();
    state.probe.cancel();
  }
}
