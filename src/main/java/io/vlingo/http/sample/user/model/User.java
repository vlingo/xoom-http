// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.sample.user.model;

import java.util.concurrent.atomic.AtomicInteger;

public class User {
  private static AtomicInteger NextId = new AtomicInteger(0);

  public final String id;
  public final Name name;
  public final Contact contact;

  public static User nonExisting() {
    return new User(null, null, null);
  }
  
  public static User from(final Name name, final Contact contact) {
    return new User(nextId(), name, contact);
  }
  
  public User from(final String id, final Name name, final Contact contact) {
    return new User(id, name, contact);
  }

  public static void resetId() {
    NextId = new AtomicInteger(0);
  }

  public static String nextId() {
    final int id = NextId.incrementAndGet(); //UUID.randomUUID().toString();
    return String.format("%03d", id);
  }
  
  public User(final String id, final Name name, final Contact contact) {
    this.id = id;
    this.name = name;
    this.contact = contact;
  }
  
  public boolean doesNotExist() {
    return id == null;
  }

  public User withContact(final Contact contact) {
    return new User(this.id, this.name, contact);
  }

  public User withName(final Name name) {
    return new User(this.id, name, this.contact);
  }

  @Override
  public String toString() {
    return "User[id=" + id + ", name=" + name + ", contact=" + contact + "]";
  }
}
