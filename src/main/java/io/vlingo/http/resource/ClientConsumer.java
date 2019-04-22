// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.nio.ByteBuffer;

import io.vlingo.actors.Stoppable;
import io.vlingo.common.Cancellable;
import io.vlingo.common.Completes;
import io.vlingo.common.Scheduled;
import io.vlingo.http.Request;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseParser;
import io.vlingo.http.resource.Client.Configuration;
import io.vlingo.wire.channel.ResponseChannelConsumer;
import io.vlingo.wire.fdx.bidirectional.ClientRequestResponseChannel;

/**
 * The client that is a request sender and that checks for
 * responses and consumes them.
 */
public interface ClientConsumer extends ResponseChannelConsumer, Scheduled<Object>, Stoppable {
  /**
   * Answer the {@code Completes<Response>} leading to the eventual outcome of the {@code request}.
   * @param request the Request being made
   * @param completes the {@code Completes<Response>}
   * @return {@code Completes<Response>}
   */
  Completes<Response> requestWith(final Request request, final Completes<Response> completes);

  /**
   * The state of a {@code ClientConsumer}.
   */
  static final class State {
    final ByteBuffer buffer;
    final ClientRequestResponseChannel channel;
    final Configuration configuration;
    ResponseParser parser;
    final Cancellable probe;

    State(
            final Configuration configuration,
            final ClientRequestResponseChannel channel,
            final ResponseParser parser,
            final Cancellable probe,
            final ByteBuffer buffer) {
      this.configuration = configuration;
      this.channel = channel;
      this.parser = parser;
      this.probe = probe;
      this.buffer = buffer;
    }
  }
}
