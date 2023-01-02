// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Actions {
  private int currentId;
  private final List<Action> actions;

  public static Actions canBe(final String method, final String uri, final String to) {
    return new Actions(method, uri, to, null);
  }

  public static Actions canBe(final String method, final String uri, final String to, final String mapper) {
    return new Actions(method, uri, to, mapper);
  }

  public Actions also(final String method, final String uri, final String to) {
    actions.add(new Action(currentId++, method, uri, to, null));
    return this;
  }

  public Actions also(final String method, final String uri, final String to, final String mapper) {
    actions.add(new Action(currentId++, method, uri, to, mapper));
    return this;
  }

  public List<Action> thatsAll() {
    return Collections.unmodifiableList(actions);
  }

  private Actions(final String method, final String uri, final String to, final String mapper) {
    this.actions = new ArrayList<>();
    actions.add(new Action(currentId++, method, uri, to, mapper));
  }
}
