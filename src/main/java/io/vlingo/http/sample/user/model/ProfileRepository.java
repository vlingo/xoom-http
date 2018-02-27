// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.sample.user.model;

import java.util.HashMap;
import java.util.Map;

public class ProfileRepository {
  private final Map<String,Profile> profiles;
  
  public ProfileRepository() {
    this.profiles = new HashMap<>();
  }
  
  public Profile profileOf(final String userId) {
    final Profile profile = profiles.get(userId);
    
    return profile == null ? Profile.nonExisting() : profile;
  }
  
  public void save(final Profile profile) {
    profiles.put(profile.id, profile);
  }
}
