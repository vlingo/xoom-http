// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.sample.user;

import static io.vlingo.common.serialization.JsonSerialization.serialized;
import static io.vlingo.http.Response.Status.Created;
import static io.vlingo.http.Response.Status.NotFound;
import static io.vlingo.http.Response.Status.Ok;
import static io.vlingo.http.ResponseHeader.Location;
import static io.vlingo.http.ResponseHeader.headers;
import static io.vlingo.http.ResponseHeader.of;

import io.vlingo.actors.Definition;
import io.vlingo.actors.Stage;
import io.vlingo.actors.World;
import io.vlingo.http.Response;
import io.vlingo.http.resource.ResourceHandler;
import io.vlingo.http.sample.user.model.Profile;
import io.vlingo.http.sample.user.model.ProfileActor;
import io.vlingo.http.sample.user.model.ProfileRepository;

public class ProfileResource extends ResourceHandler {
  private final ProfileRepository repository = ProfileRepository.instance();
  private final Stage stage;

  public ProfileResource(final World world) {
    this.stage = world.stageNamed("service");
  }

  public void define(final String userId, final ProfileData profileData) {
    stage.actorOf(Profile.class, stage.world().addressFactory().findableBy(Integer.parseInt(userId)))
      .andThenConsume(profile -> {
        final Profile.State profileState = repository.profileOf(userId);
        completes().with(Response.of(Ok, headers(of(Location, profileLocation(userId))), serialized(ProfileData.from(profileState))));
      })
      .otherwiseConsume(noProfile -> {
        final Profile.State profileState =
                Profile.from(
                        userId,
                        profileData.twitterAccount,
                        profileData.linkedInAccount,
                        profileData.website);
  
        stage().actorFor(Profile.class, Definition.has(ProfileActor.class, Definition.parameters(profileState)));
  
        repository.save(profileState);
        completes().with(Response.of(Created, serialized(ProfileData.from(profileState))));
      });
  }

  public void query(final String userId) {
    final Profile.State profileState = repository.profileOf(userId);
    if (profileState.doesNotExist()) {
      completes().with(Response.of(NotFound, profileLocation(userId)));
    } else {
      completes().with(Response.of(Ok, serialized(ProfileData.from(profileState))));
    }
  }

  private String profileLocation(final String userId) {
    return "/users/" + userId + "/profile";
  }
}
