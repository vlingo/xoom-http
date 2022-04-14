// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
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

  public final Body body;
  public final ContentMediaType mediaType;
  public final ContentEncoding contentEncoding;

  public RequestData(final Body body,
                     final ContentMediaType mediaType,
                     final ContentEncoding contentEncoding) {
    this.body = body;
    this.mediaType = mediaType;
    this.contentEncoding = contentEncoding;
  }

  @Override
  public int hashCode() {
    return Objects.hash(body, mediaType);
  }
}
