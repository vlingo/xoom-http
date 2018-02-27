// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.sample.user.model;

public class Profile {
  public final String id;
  public final String linkedInAccount;
  public final String twitterAccount;
  public final String website;

  public static Profile from(final String id, final String twitterAccount, final String linkedInAccount, final String website) {
    return new Profile(id, twitterAccount, linkedInAccount, website);
  }
  
  public static Profile nonExisting() {
    return new Profile(null, null, null, null);
  }
  
  public Profile(final String id, final String twitterAccount, final String linkedInAccount, final String website) {
    this.id = id;
    this.twitterAccount = twitterAccount;
    this.linkedInAccount = linkedInAccount;
    this.website = website;
  }
  
  public boolean doesNotExist() {
    return id == null;
  }
}
