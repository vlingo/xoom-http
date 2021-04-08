// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import io.vlingo.xoom.actors.Logger;
import io.vlingo.xoom.http.resource.Client.Configuration;
import io.vlingo.xoom.wire.channel.ResponseChannelConsumer;
import io.vlingo.xoom.wire.fdx.bidirectional.ClientRequestResponseChannel;
import io.vlingo.xoom.wire.fdx.bidirectional.SecureClientRequestResponseChannel;
import io.vlingo.xoom.wire.fdx.bidirectional.netty.client.NettyClientRequestResponseChannel;

/**
 * Common behaviors needed by all {@code ClientConsumer} implementations.
 */
public class ClientConsumerCommons {
  /**
   * Answer a new ClientRequestResponseChannel from the {@code configuration}.
   * @param configuration the Configuration
   * @return ClientRequestResponseChannel
   * @throws Exception when the channel cannot be created
   */
  static ClientRequestResponseChannel clientChannel(
          final Configuration configuration,
          final ResponseChannelConsumer consumer,
          final Logger logger) throws Exception {

    if (configuration.secure) {
      return new SecureClientRequestResponseChannel(
              configuration.addressOfHost,
              consumer,
              configuration.readBufferPoolSize,
              configuration.readBufferSize,
              logger);
    } else {
      return new NettyClientRequestResponseChannel(
              configuration.addressOfHost,
              consumer,
              configuration.readBufferPoolSize,
              configuration.readBufferSize);
    }
  }
}
