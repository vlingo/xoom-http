// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.xoom.http;


import io.vlingo.xoom.http.media.ContentMediaType;

import java.util.Objects;

/***
 * Provides an interface to the request body as an alternative to trying to deserialize
 * and provide the resource handler with the resulting object.
 */
public class RequestData {

  private final Body body;
  private final ContentMediaType mediaType;
  private ContentEncoding contentEncoding;

  public RequestData(final Body body,
                     final ContentMediaType mediaType,
                     final ContentEncoding contentEncoding) {
    this.body = body;
    this.mediaType = mediaType;
    this.contentEncoding = contentEncoding;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RequestData that = (RequestData) o;
    return Objects.equals(body, that.body) &&
      Objects.equals(mediaType, that.mediaType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(body, mediaType);
  }
}
