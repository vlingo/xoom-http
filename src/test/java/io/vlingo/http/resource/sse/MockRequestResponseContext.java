// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.sse;

import java.util.concurrent.atomic.AtomicReference;

import io.vlingo.wire.channel.RequestResponseContext;
import io.vlingo.wire.channel.ResponseSenderChannel;

public class MockRequestResponseContext implements RequestResponseContext<String> {
  public final MockResponseSenderChannel channel;
  public AtomicReference<Object> consumerData = new AtomicReference<>();
  public AtomicReference<Object> whenClosingData = new AtomicReference<>();

  public MockRequestResponseContext(final MockResponseSenderChannel channel) {
    this.channel = channel;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T consumerData() {
    return (T) consumerData.get();
  }

  @Override
  public <T> T consumerData(final T data) {
    consumerData.set(data);
    return data;
  }

  @Override
  public boolean hasConsumerData() {
    return consumerData.get() != null;
  }

  @Override
  public String id() {
    return "1";
  }

  @Override
  public ResponseSenderChannel sender() {
    return channel;
  }

  @Override
  public void whenClosing(final Object data) {
    whenClosingData.set(data);
  }
}
