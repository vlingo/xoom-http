// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Actions {
  private int currentId;
  private final List<Action> actions;

  public static Actions canBe(final String method, final String uri, final String to, final boolean disallowPathParametersWithSlash) {
    return new Actions(method, uri, to, null, disallowPathParametersWithSlash);
  }

  public static Actions canBe(final String method, final String uri, final String to, final String mapper, final boolean disallowPathParametersWithSlash) {
    return new Actions(method, uri, to, mapper, disallowPathParametersWithSlash);
  }

  public Actions also(final String method, final String uri, final String to, final boolean disallowPathParametersWithSlash) {
    actions.add(new Action(currentId++, method, uri, to, null, disallowPathParametersWithSlash));
    return this;
  }

  public Actions also(final String method, final String uri, final String to, final String mapper, final boolean disallowPathParametersWithSlash) {
    actions.add(new Action(currentId++, method, uri, to, mapper, disallowPathParametersWithSlash));
    return this;
  }

  public List<Action> thatsAll() {
    return Collections.unmodifiableList(actions);
  }

  private Actions(final String method, final String uri, final String to, final String mapper, final boolean disallowPathParametersWithSlash) {
    this.actions = new ArrayList<>();
    actions.add(new Action(currentId++, method, uri, to, mapper, disallowPathParametersWithSlash));
  }
}
