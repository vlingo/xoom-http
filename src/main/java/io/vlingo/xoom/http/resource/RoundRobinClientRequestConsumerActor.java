// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import io.vlingo.xoom.actors.RoundRobinRouter;
import io.vlingo.xoom.actors.RouterSpecification;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.common.Scheduled;
import io.vlingo.xoom.http.Request;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.resource.Client.Configuration;
import io.vlingo.xoom.wire.message.ConsumerByteBuffer;

/**
 * Round-robin router of `ClientConsumer` requests.
 */
public class RoundRobinClientRequestConsumerActor extends RoundRobinRouter<ClientConsumer> implements ClientConsumer {
  private static String ErrorMessage = "RoundRobinClientRequestConsumerActor: Should not be reached. Message: ";

  /**
   * Constructs my default state.
   * @param configuration the Configuration
   * @param specification the RouterSpecification
   * @throws Exception when the router cannot be initialized
   */
  public RoundRobinClientRequestConsumerActor(
          final Configuration configuration,
          final RouterSpecification<ClientConsumer> specification) throws Exception {
    super(specification);
  }

  /**
   * @see io.vlingo.xoom.wire.channel.ResponseChannelConsumer#consume(io.vlingo.xoom.wire.message.ConsumerByteBuffer)
   */
  @Override
  public void consume(final ConsumerByteBuffer buffer) {
    // no-op
    final String message = ErrorMessage + "consume()";
    logger().error(message, new UnsupportedOperationException(message));
  }

  /**
   * @see io.vlingo.xoom.common.Scheduled#intervalSignal(io.vlingo.xoom.common.Scheduled, java.lang.Object)
   */
  @Override
  public void intervalSignal(final Scheduled<Object> scheduled, final Object data) {
    // no-op
    final String message = ErrorMessage + "intervalSignal()";
    logger().error(message, new UnsupportedOperationException(message));
  }

  /**
   * @see io.vlingo.xoom.http.resource.ClientConsumer#requestWith(io.vlingo.xoom.http.Request, io.vlingo.xoom.common.Completes)
   */
  @Override
  public Completes<Response> requestWith(final Request request, final Completes<Response> completes) {
    dispatchCommand(ClientConsumer::requestWith, request, completes);
    return completes;
  }
}
