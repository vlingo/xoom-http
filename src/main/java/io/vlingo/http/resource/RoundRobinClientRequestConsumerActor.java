// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import io.vlingo.actors.RoundRobinRouter;
import io.vlingo.actors.RouterSpecification;
import io.vlingo.common.Completes;
import io.vlingo.common.Scheduled;
import io.vlingo.http.Request;
import io.vlingo.http.Response;
import io.vlingo.http.resource.Client.Configuration;
import io.vlingo.wire.message.ConsumerByteBuffer;

/**
 * Round-robin router of `ClientConsumer` requests.
 */
public class RoundRobinClientRequestConsumerActor extends RoundRobinRouter<ClientConsumer> implements ClientConsumer {

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
   * @see io.vlingo.wire.channel.ResponseChannelConsumer#consume(io.vlingo.wire.message.ConsumerByteBuffer)
   */
  @Override
  public void consume(final ConsumerByteBuffer buffer) {
    // no-op
  }

  /**
   * @see io.vlingo.common.Scheduled#intervalSignal(io.vlingo.common.Scheduled, java.lang.Object)
   */
  @Override
  public void intervalSignal(final Scheduled<Object> scheduled, final Object data) {
    // no-op
  }

  /**
   * @see io.vlingo.http.resource.ClientConsumer#requestWith(io.vlingo.http.Request, io.vlingo.common.Completes)
   */
  @Override
  public Completes<Response> requestWith(final Request request, final Completes<Response> completes) {
    dispatchCommand(ClientConsumer::requestWith, request, completes);
    return completes;
  }
}
