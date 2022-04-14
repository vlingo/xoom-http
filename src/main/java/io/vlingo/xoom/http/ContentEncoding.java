// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http;

import java.util.Arrays;
import java.util.Optional;

/***
 * Contains the ordered list of content encodings that have been applied to a piece of
 * content.  To reverse any such compresseion, these encodings should be applied in reverse order
 */
public class ContentEncoding {

  public final ContentEncodingMethod[] encodingMethods;

  public ContentEncoding(ContentEncodingMethod ... encodingMethods) {
    this.encodingMethods = encodingMethods;
  }

  public ContentEncoding() {
    encodingMethods = new ContentEncodingMethod[0];
  }

  public static ContentEncoding parseFromHeader(final String headerValue) {
    String[] methods = headerValue.split(",");
    return new ContentEncoding(Arrays
      .stream(methods)
      .map(m -> ContentEncodingMethod.parse(m.trim()))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .toArray(ContentEncodingMethod[]::new));
  }

  public static ContentEncoding none() {
    return new ContentEncoding();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ContentEncoding that = (ContentEncoding) o;
    return Arrays.equals(encodingMethods, that.encodingMethods);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(encodingMethods);
  }
}
