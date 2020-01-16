/*
 * Copyright © 2012-2020 VLINGO LABS. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http;

/**
 * An HTTP compliant content type.
 *
 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Type
 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types
 * https://tools.ietf.org/html/rfc7231#section-3.1.1.1
 * https://tools.ietf.org/html/rfc7231#section-3.1.1.5
 */
public class ContentType {
  public final String mediaType;
  public final String charset;
  public final String boundary;

  /**
   * Answer a new ContentType with only a {@code mediaType}.
   * @param mediaType the String media type
   * @return ContentType
   */
  public static ContentType of(final String mediaType) {
    return new ContentType(mediaType, "", "");
  }

  /**
   * Answer a new ContentType with only a {@code mediaType} and {@code charset}.
   * @param mediaType the String media type
   * @param charset the String character set
   * @return ContentType
   */
  public static ContentType of(final String mediaType, final String charset) {
    return new ContentType(mediaType, charset, "");
  }

  /**
   * Answer a new ContentType with all of {@code mediaType}, {@code charset}, and {@code boundary}.
   * @param mediaType the String media type
   * @param charset the String character set
   * @param boundary the String boundary
   * @return ContentType
   */
  public static ContentType of(final String mediaType, final String charset, final String boundary) {
    return new ContentType(mediaType, charset, boundary);
  }

  /**
   * Answer myself as a {@code RequestHeader}.
   * @return RequestHeader
   */
  public RequestHeader toRequestHeader() {
    return RequestHeader.contentType(toString());
  }

  /**
   * Answer myself as a {@code ResponseHeader}.
   * @return ResponseHeader
   */
  public ResponseHeader toResponseHeader() {
    return ResponseHeader.contentType(toString());
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    if (!mediaType.isEmpty()) {
      builder.append(mediaType);
      if (!charset.isEmpty()) {
        builder.append("; ").append(charset);
      }
      if (!boundary.isEmpty()) {
        builder.append("; ").append(boundary);
      }
    }
    return builder.toString();
  }

  private ContentType(final String mediaType, final String charset, final String boundary) {
    this.mediaType = mediaType;
    this.charset = charset;
    this.boundary = boundary;
  }
}
