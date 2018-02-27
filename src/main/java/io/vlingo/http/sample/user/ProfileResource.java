// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.sample.user;

import static io.vlingo.http.Response.Created;
import static io.vlingo.http.Response.NotFound;
import static io.vlingo.http.Response.Ok;
import static io.vlingo.http.ResponseHeader.Location;
import static io.vlingo.http.ResponseHeader.headers;
import static io.vlingo.http.ResponseHeader.of;
import static io.vlingo.http.resource.serialization.JsonSerialization.serialized;

import io.vlingo.http.Header.Headers;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseHeader;
import io.vlingo.http.resource.ResourceHandler;
import io.vlingo.http.sample.user.model.Profile;
import io.vlingo.http.sample.user.model.ProfileRepository;

public class ProfileResource extends ResourceHandler {
  private final ProfileRepository repository;

  public ProfileResource() {
    this.repository = new ProfileRepository();
  }

  public void define(final String userId, final ProfileData profileData) {
    final Profile maybeProfile = repository.profileOf(userId);
    
    final String statusCode = maybeProfile.doesNotExist() ? Created : Ok;
    
    final Headers<ResponseHeader> headers = maybeProfile.doesNotExist() ?
                    headers(of(Location, profileLocation(userId))) :
                    Headers.empty();
    
    final Profile profile = Profile.from(userId, profileData.twitterAccount, profileData.linkedInAccount, profileData.website);
    
    repository.save(profile);
    
    completes().with(Response.of(statusCode, headers, serialized(ProfileData.from(profile))));
  }

  public void query(final String userId) {
    final Profile profile = repository.profileOf(userId);
    if (profile.doesNotExist()) {
      completes().with(Response.of(NotFound, profileLocation(userId)));
    } else {
      completes().with(Response.of(Ok, serialized(ProfileData.from(profile))));
    }
  }

  private String profileLocation(final String userId) {
    return "/users/" + userId + "/profile";
  }
}
