// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import java.util.Collections;
import java.util.List;

public class Body {
  public final List<String> lines;
  
  public static Body from(final List<String> lines) {
    return new Body(lines);
  }

  Body(final List<String> lines) {
    this.lines = Collections.unmodifiableList(lines);
  }

  public boolean hasLines() {
    return !lines.isEmpty();
  }

  public String line(final int index) {
    return lines.get(index);
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    for (final String line : lines) {
      builder.append(line);
    }
    return builder.toString();
  }
}
