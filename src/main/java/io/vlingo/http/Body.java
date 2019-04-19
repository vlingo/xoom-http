// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Base64;

/**
 * An HTTP request/response body, with concrete subclasses {@code BinaryBody} and {@code TextBody}.
 */
public class Body {
  public enum Encoding { Base64, UTF8 };

  /** An empty body. */
  public static final Body Empty = new Body();

  /** My content. */
  public final String content;

  /**
   * Answer the {@code Empty} Body.
   * @return Body
   */
  public static Body empty() {
    return Empty;
  }

  /**
   * Answer a new {@code Body} with binary content using {@code encoding}.
   * @param body the byte[] content
   * @param encoding the Encoding to use
   * @return Body
   */
  public static Body from(final byte[] body, final Encoding encoding) {
    switch (encoding) {
    case Base64:
      return new Body(bytesToBase64(body));
    case UTF8:
      return new Body(bytesToUTF8(body));
    }
    throw new IllegalArgumentException("Unmapped encoding: " + encoding);
  }

  /**
   * Answer a new {@code Body} with binary content using {@code encoding}.
   * @param body the ByteBuffer content
   * @param encoding the Encoding to use
   * @return Body
   */
  public static Body from(final ByteBuffer body, final Encoding encoding) {
    switch (encoding) {
    case Base64:
      return new Body(bytesToBase64(bufferToArray(body)));
    case UTF8:
      return new Body(bytesToUTF8(bufferToArray(body)));
    }
    throw new IllegalArgumentException("Unmapped encoding: " + encoding);
  }

  /**
   * Answer a new {@code Body} with binary content encoded as a Base64 {@code String}.
   * @param body the byte[] content
   * @return Body
   */
  public static Body from(final byte[] body) {
    return from(body, Encoding.Base64);
  }

  /**
   * Answer a new {@code Body} with binary content encoded as a Base64 {@code String}.
   * @param body the ByteBuffer content
   * @return Body
   */
  public static Body from(final ByteBuffer body) {
    return new Body(bytesToBase64(bufferToArray(body)));
  }

  /**
   * Answer a new {@code Body} with text content, which is a {@code TextBody}.
   * @param body the String content
   * @return Body
   */
  public static Body from(final String body) {
    return new Body(body);
  }

  /**
   * Answer a {@code byte[]} from {@code body} bytes.
   * @param body the ByteBuffer
   * @return String
   */
  private static byte[] bufferToArray(final ByteBuffer body) {
    if (body.position() > 0) {
      body.flip();
    }
    final int length = body.limit();
    final byte[] bytes = new byte[length];
    System.arraycopy(body.array(), 0, bytes, 0, length);
    return bytes;
  }

  /**
   * Answer a Base64 {@code String} from {@code body} bytes.
   * @param body the byte[]
   * @return String
   */
  private static String bytesToBase64(final byte[] body) {
    final String encoded = Base64.getEncoder().encodeToString(body);
    return encoded;
  }

  /**
   * Answer a UTF-8 {@code String} from {@code body} bytes.
   * @param body the byte[]
   * @return String
   */
  private static String bytesToUTF8(final byte[] body) {
    final String encoded = new String(body, Charset.forName("UTF-8"));
    return encoded;
  }

  /**
   * Answer whether or not I have content.
   * @return boolean
   */
  public boolean hasContent() {
    return !content.isEmpty();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return content;
  }

  /**
   * Construct my default state with the {@code body} as content.
   * @param body the String body content
   */
  Body(final String body) {
    this.content = body;
  }

  /**
   * Construct my default state with empty body content.
   */
  Body() {
    this.content = "";
  }
}
