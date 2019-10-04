// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import java.nio.charset.StandardCharsets;

public class BinaryBody implements Body {

  public final byte[] binaryContent;

  @Override
  public String content() {
    return new String(binaryContent, StandardCharsets.UTF_8);
  }

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
}
