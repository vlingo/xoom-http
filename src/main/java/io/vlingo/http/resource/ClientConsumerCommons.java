// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import io.vlingo.actors.Logger;
import io.vlingo.http.resource.Client.Configuration;
import io.vlingo.wire.channel.ResponseChannelConsumer;
import io.vlingo.wire.fdx.bidirectional.ClientRequestResponseChannel;

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
    return new ClientRequestResponseChannel(
            configuration.addressOfHost,
            consumer,
            configuration.readBufferPoolSize,
            configuration.readBufferSize,
            logger);
  }
}
