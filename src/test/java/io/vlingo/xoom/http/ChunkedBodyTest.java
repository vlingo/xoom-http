// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ChunkedBodyTest {
  private static final String Chunk1 = "ABCDEFGHIJKLMNOPQRSTUVWYYZ0123";
  private static final String Chunk2 = "abcdefghijklmnopqrstuvwxyz012345";

  @Test
  public void testThatChunkedBodyChunks() {
    final ChunkedBody body =
            Body
              .beginChunked()
              .appendChunk(Chunk1)
              .appendChunk(Chunk2)
              .end();

    assertTrue(body.content().contains(asChunk(Chunk1)));
    assertTrue(body.content().contains(asChunk(Chunk2)));
  }

  private String asChunk(final String content) {
    return Integer.toHexString(content.length()) + "\r\n" + content + "\r\n";
  }
}
