// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import java.util.Collections;
import java.util.List;

public class HttpBody {
  public final List<String> lines;
  
  public static HttpBody from(final List<String> lines) {
    return new HttpBody(lines);
  }

  HttpBody(final List<String> lines) {
    this.lines = Collections.unmodifiableList(lines);
  }

  public boolean hasLines() {
    return !lines.isEmpty();
  }

  public String line(final int index) {
    return lines.get(index);
  }
}
