// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Base64;

/**
 * An HTTP request/response body, with concrete subclass {@code PlainBody} and {@code ChunkedBody}.
 */
public interface Body {
  enum Encoding { Base64, UTF8, None };

  /** An empty {@code PlainBody}. */
  static final Body Empty = new PlainBody();

  /**
   * Answer the {@code ChunkedBody} prepared to receive chunks of content.
   * @return ChunkedBody
   */
  static ChunkedBody beginChunked() {
    return new ChunkedBody();
  }

  /**
   * Answer a new {@code ChunkedBody} with {@code Body} content as the initial chunk.
   * @param body the Body content to add as a chunk
   * @return ChunkedBody
   */
  static ChunkedBody beginChunkedWith(final Body body) {
    return Body.beginChunked().appendChunk(body);
  }

  /**
   * Answer a new {@code ChunkedBody} with the {@code content} as the initial chunk.
   * @param content the String content
   * @return ChunkedBody
   */
  static ChunkedBody beginChunkedWith(final String content) {
    return Body.beginChunked().appendChunk(content);
  }

  /**
   * Answer the {@code Empty} Body.
   * @return Body
   */
  static Body empty() {
    return Empty;
  }

  /**
   * Answer a new {@code Body} with binary content using {@code encoding}.
   * @param body the byte[] content
   * @param encoding the Encoding to use
   * @return Body
   */
  static Body from(final byte[] body, final Encoding encoding) {
    switch (encoding) {
    case Base64:
      return new PlainBody(bytesToBase64(body));
    case UTF8:
      return new PlainBody(bytesToUTF8(body));
    case None:
      return new BinaryBody(body);
    }
    throw new IllegalArgumentException("Unmapped encoding: " + encoding);
  }

  /**
   * Answer a new {@code Body} with binary content using {@code encoding}.
   * @param body the ByteBuffer content
   * @param encoding the Encoding to use
   * @return Body
   */
  static Body from(final ByteBuffer body, final Encoding encoding) {
    switch (encoding) {
    case Base64:
      return new PlainBody(bytesToBase64(bufferToArray(body)));
    case UTF8:
      return new PlainBody(bytesToUTF8(bufferToArray(body)));
    case None:
      return new BinaryBody(bufferToArray(body));
    }
    throw new IllegalArgumentException("Unmapped encoding: " + encoding);
  }

  /**
   * Answer a new {@code Body} with binary content encoded as a Base64 {@code String}.
   * @param body the byte[] content
   * @return Body
   */
  static Body from(final byte[] body) {
    return from(body, Encoding.Base64);
  }

  /**
   * Answer a new {@code Body} with binary content encoded as a Base64 {@code String}.
   * @param body the ByteBuffer content
   * @return Body
   */
  static Body from(final ByteBuffer body) {
    return new PlainBody(bytesToBase64(bufferToArray(body)));
  }

  /**
   * Answer a new {@code Body} with text content, which is a {@code TextBody}.
   * @param body the String content
   * @return Body
   */
  static PlainBody from(final String body) {
    return new PlainBody(body);
  }

  /**
   * Answer my content as a {@code String}.
   * @return String
   */
  String content();

  /**
   * Answer my content as a {@code byte[]}.
   * @return byte[]
   */
  byte[] binaryContent();

  /**
   * Answer whether or not this {@code Body} content is complex.
   * A {@code PlainBody} is not complex. A {@code ChunkedBody} is complex.
   * @return boolean
   */
  default boolean isComplex() { return false; }

  /**
   * Answer whether or not I have content.
   * @return boolean
   */
  boolean hasContent();


  /**
   * Answer a {@code byte[]} from {@code body} bytes.
   * @param body the ByteBuffer
   * @return String
   */
  static byte[] bufferToArray(final ByteBuffer body) {
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
  static String bytesToBase64(final byte[] body) {
    final String encoded = Base64.getEncoder().encodeToString(body);
    return encoded;
  }

  /**
   * Answer a UTF-8 {@code String} from {@code body} bytes.
   * @param body the byte[]
   * @return String
   */
  static String bytesToUTF8(final byte[] body) {
    final String encoded = new String(body, Charset.forName("UTF-8"));
    return encoded;
  }
}
