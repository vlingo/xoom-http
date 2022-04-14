// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http;

import java.util.Optional;

public enum ContentEncodingMethod {

  GZIP("gzip"),
  COMPRESS("compress"),
  DEFLATE("deflate"),
  BROTLI("br");

  public final String descriptor;

  private ContentEncodingMethod(final String descriptor) {
    this.descriptor = descriptor;
  }

  public static Optional<ContentEncodingMethod> parse(final String value) {
    for (ContentEncodingMethod method : ContentEncodingMethod.values()) {
      if (value.compareTo(method.descriptor) == 0) {
        return Optional.of(method);
      }
    }
    return Optional.empty();
  }

}
