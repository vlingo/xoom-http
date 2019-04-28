// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.vlingo.actors.Actor;
import io.vlingo.actors.CompletesEventually;
import io.vlingo.common.Completes;
import io.vlingo.common.Scheduled;
import io.vlingo.http.Request;
import io.vlingo.http.RequestHeader;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseHeader;
import io.vlingo.http.ResponseParser;
import io.vlingo.http.resource.Client.Configuration;
import io.vlingo.wire.channel.ResponseChannelConsumer;
import io.vlingo.wire.message.ByteBufferAllocator;
import io.vlingo.wire.message.ConsumerByteBuffer;
import io.vlingo.wire.message.Converters;

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

    while (state.parser.hasFullResponse()) {
      final Response response = state.parser.fullResponse();
      final ResponseHeader correlationId = response.headers.headerOf(ResponseHeader.XCorrelationID);
      if (correlationId == null) {
        logger().log("Client Consumer: Cannot complete response because no correlation id.");
        state.configuration.consumerOfUnknownResponses.consume(response);
      } else {
        final CompletesEventually completes = state.configuration.keepAlive ?
                completables.get(correlationId.value) :
                completables.remove(correlationId.value);
        if (completes == null) {
          state.configuration.stage.world().defaultLogger().log(
                  "Client Consumer: Cannot complete response because mismatched correlation id: " +
                   correlationId.value);
          state.configuration.consumerOfUnknownResponses.consume(response);
        } else {
          completes.with(response);
        }
      }
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
    RequestHeader correlationId = request.headers.headerOf(RequestHeader.XCorrelationID);

    final Request readyRequest;

    if (correlationId == null) {
      correlationId = RequestHeader.of(RequestHeader.XCorrelationID, UUID.randomUUID().toString());
      readyRequest = request.and(correlationId);
    } else {
      readyRequest = request;
    }

    completables.put(correlationId.value, stage().world().completesFor(completes));

    state.buffer.clear();
    state.buffer.put(Converters.textToBytes(readyRequest.toString()));
    state.buffer.flip();
    state.channel.requestWith(state.buffer);

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
