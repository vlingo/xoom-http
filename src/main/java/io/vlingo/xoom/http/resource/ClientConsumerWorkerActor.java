// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.CompletesEventually;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Returns;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.http.Request;
import io.vlingo.xoom.http.RequestHeader;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.ResponseHeader;
import io.vlingo.xoom.http.ResponseParser;
import io.vlingo.xoom.http.resource.Client.Configuration;
import io.vlingo.xoom.http.resource.RequestSender.RequestSenderProbeInstantiator;
import io.vlingo.xoom.wire.channel.ResponseChannelConsumer;
import io.vlingo.xoom.wire.message.ConsumerByteBuffer;

/**
 * Common behavior implemented by the worker fulfilling the {@code ClientConsumer} contract.
 */
public class ClientConsumerWorkerActor extends Actor implements ClientConsumer {
  private static final String EmptyTestId = "";
  private static final AtomicInteger testIdGenerator = new AtomicInteger(0);

  private final String testId;

  private CompletesEventually completesEventually;
  private ResponseParser parser;
  private final RequestSender requestSender;

  /**
   * Constructs my default state.
   * @param configuration the Configuration
   * @throws Exception when the ClientConsumer cannot be created
   */
  public ClientConsumerWorkerActor(final Configuration configuration) throws Exception {
    this.testId = configuration.hasTestInfo() ?
            Integer.toString(testIdGenerator.incrementAndGet()) :
            EmptyTestId;

    this.requestSender = startRequestSender(configuration);

    this.parser = null;
  }

  /**
   * @see io.vlingo.xoom.wire.channel.ResponseChannelConsumer#consume(io.vlingo.xoom.wire.message.ConsumerByteBuffer)
   */
  @Override
  public void consume(final ConsumerByteBuffer buffer) {
    try {
      final ByteBuffer parsable = buffer.asByteBuffer();
      if (!parsable.hasRemaining()) {
        logger().debug("CONSUMER: NO CONTENT");
        return;
      }
      logger().debug("CONSUMER:\n" + new String(parsable.array(), 0, parsable.remaining()));
      if (parser == null) {
        parser = ResponseParser.parserFor(parsable);
      } else {
        parser.parseNext(parsable);
      }

      // don't disperse stowed messages unless a full response has arrived
      if (parser.hasFullResponse()) {
        final Response response = parser.fullResponse();

        if (testId != EmptyTestId) {
          response.headers.add(ResponseHeader.of(Client.ClientIdCustomHeader, testId));
          logger().debug("Client Worker: " + testId + " Consuming");
          logger().debug("Client Worker: " + testId + "\nConsuming:\n" + response);
        }

        completesEventually.with(response);

        completesEventually = null;

        disperseStowedMessages();
      }

      if (!parser.isMissingContent()) {
        parser = null;
      }
    } finally {
      buffer.release();
    }
  }

  /**
   * @see io.vlingo.xoom.http.resource.ClientConsumer#requestWith(io.vlingo.xoom.http.Request, io.vlingo.xoom.common.Completes)
   */
  @Override
  public Completes<Response> requestWith(final Request request, final Completes<Response> completes) {
    completesEventually = stage().world().completesFor(Returns.value(completes));

    if (testId != EmptyTestId) {
      request.headers.add(RequestHeader.of(Client.ClientIdCustomHeader, testId));
      request.headers.add(RequestHeader.of(RequestHeader.XCorrelationID, testId));
      logger().debug("Client Worker: " + testId + " Requesting");
      logger().debug("Client Worker: " + testId + "\nRequesting:\n" + request);
    }

    requestSender.sendRequest(request);

    stowMessages(ResponseChannelConsumer.class);

    return completes;
  }

  /**
   * @see io.vlingo.xoom.actors.Stoppable#stop()
   */
  @Override
  public void stop() {
    requestSender.stop();

    super.stop();
  }

  private RequestSender startRequestSender(final Configuration configuration) throws Exception {
    final ResponseChannelConsumer self = selfAs(ResponseChannelConsumer.class);

    final Definition definition =
            Definition.has(
                    RequestSenderProbeActor.class,
                    new RequestSenderProbeInstantiator(configuration, self, testId));

    RequestSender requestSender = childActorFor(RequestSender.class, definition);

    return requestSender;
  }
}
