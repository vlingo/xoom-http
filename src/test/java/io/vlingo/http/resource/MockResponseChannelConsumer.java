// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseParser;
import io.vlingo.wire.channel.ResponseChannelConsumer;

public class MockResponseChannelConsumer implements ResponseChannelConsumer {
  public static TestUntil untilConsumed;
  
  public List<Response> responses = new ArrayList<>();
  public int consumeCount;
  
  @Override
  public void consume(final ByteBuffer buffer) {
    final Response response = ResponseParser.parse(buffer);
    ++consumeCount;
    responses.add(response);
    if (untilConsumed != null) untilConsumed.happened();
  }
}
