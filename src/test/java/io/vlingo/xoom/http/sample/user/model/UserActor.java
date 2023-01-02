// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.sample.user.model;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.common.Completes;

public class UserActor extends Actor implements User {
  private User.State state;

  public UserActor(final User.State state) {
    this.state = state;
  }

  @Override
  public Completes<User.State> withContact(final Contact contact) {
    state = state.withContact(contact);
    return completes().with(state);
  }

  @Override
  public Completes<User.State> withName(final Name name) {
    state = state.withName(name);
    return completes().with(state);
  }
}
