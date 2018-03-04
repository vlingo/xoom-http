// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import java.nio.ByteBuffer;

import io.vlingo.wire.message.ByteBufferAllocator;
import io.vlingo.wire.message.Converters;

public abstract class BaseTest {
  private final ByteBuffer buffer = ByteBufferAllocator.allocate(1024);

  protected ByteBuffer toByteBuffer(final String requestContent) {
    buffer.clear();
    buffer.put(Converters.textToBytes(requestContent));
    buffer.flip();
    return buffer;
  }
}
