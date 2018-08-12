// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.sample.user.model;

import java.util.concurrent.atomic.AtomicInteger;

import io.vlingo.actors.Completes;

public interface User {
  Completes<User.State> withContact(final Contact contact);
  Completes<User.State> withName(final Name name);

  static State nonExisting() {
    return new State(null, null, null);
  }

  static State from(final Name name, final Contact contact) {
    return new State(nextId(), name, contact);
  }

  static State from(final String id, final Name name, final Contact contact) {
    return new State(id, name, contact);
  }

  static void resetId() {
    State.NextId.set(0);
  }

  public static String nextId() {
    final int id = State.NextId.incrementAndGet();
    return String.format("%03d", id);
  }

  public static final class State {
    private static final AtomicInteger NextId = new AtomicInteger(0);

    public final String id;
    public final Name name;
    public final Contact contact;
    
    public boolean doesNotExist() {
      return id == null;
    }

    public State withContact(final Contact contact) {
      return new State(this.id, this.name, contact);
    }

    public State withName(final Name name) {
      return new State(this.id, name, this.contact);
    }

    @Override
    public String toString() {
      return "User.State[id=" + id + ", name=" + name + ", contact=" + contact + "]";
    }

    private State(final String id, final Name name, final Contact contact) {
      this.id = id;
      this.name = name;
      this.contact = contact;
    }
  }
}
