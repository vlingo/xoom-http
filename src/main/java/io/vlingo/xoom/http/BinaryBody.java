// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http;

import java.util.Arrays;

public class BinaryBody implements Body {

  public final byte[] binaryContent;

  @Override
  public String content() {
    return Body.bytesToBase64(binaryContent);
  }

  @Override
  public byte[] binaryContent() {
    return binaryContent;
  }

  @Override
  public boolean hasContent() {
    return !(binaryContent.length == 0);
  }

  @Override
  public String toString() {
    return content();
  }


  BinaryBody(final byte[] body) {
    this.binaryContent = body;
  }

  BinaryBody() {
    this.binaryContent = new byte[0];
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BinaryBody that = (BinaryBody) o;
    return Arrays.equals(binaryContent, that.binaryContent);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(binaryContent);
  }
}
