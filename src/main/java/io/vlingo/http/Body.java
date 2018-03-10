// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

public class Body {
  public final String content;
  
  public static Body from(final String body) {
    return new Body(body);
  }

  Body(final String body) {
    this.content = body;
  }

  public boolean hasContent() {
    return !content.isEmpty();
  }

  @Override
  public String toString() {
    return content;
  }
}
