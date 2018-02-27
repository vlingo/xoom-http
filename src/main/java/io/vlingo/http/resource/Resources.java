// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.util.Collections;
import java.util.Map;

public class Resources {
  final Map<String,Resource<?>> namedResources;
  
  Resources(final Map<String,Resource<?>> namedResources) {
    this.namedResources = Collections.unmodifiableMap(namedResources);
  }

  Resource<?> resourceOf(final String name) {
    return namedResources.get(name);
  }
}
