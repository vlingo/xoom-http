// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.sample.user.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserRepository {
  private static UserRepository instance;
  
  private final Map<String,User> users;

  public static synchronized UserRepository instance() {
    if (instance == null) {
      instance = new UserRepository();
    }
    return instance;
  }

  public static void reset() {
    instance = null;
  }

  public UserRepository() {
    this.users = new ConcurrentHashMap<>();
  }

  public User userOf(final String userId) {
    final User user = users.get(userId);
    
    return user == null ? User.nonExisting() : user;
  }

  public Collection<User> users() {
    return Collections.unmodifiableCollection(users.values());
  }

  public void save(final User user) {
    users.put(user.id, user);
  }
}
