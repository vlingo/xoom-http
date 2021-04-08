// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.sample.user;

import static io.vlingo.xoom.common.serialization.JsonSerialization.serialized;
import static io.vlingo.xoom.http.Response.Status.Created;
import static io.vlingo.xoom.http.Response.Status.NotFound;
import static io.vlingo.xoom.http.Response.Status.Ok;
import static io.vlingo.xoom.http.ResponseHeader.Location;
import static io.vlingo.xoom.http.ResponseHeader.headers;
import static io.vlingo.xoom.http.ResponseHeader.of;

import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.resource.ResourceHandler;
import io.vlingo.xoom.http.sample.user.model.Profile;
import io.vlingo.xoom.http.sample.user.model.ProfileActor;
import io.vlingo.xoom.http.sample.user.model.ProfileRepository;

public class ProfileResource extends ResourceHandler {
  private final ProfileRepository repository = ProfileRepository.instance();
  private final Stage stage;

  public ProfileResource(final World world) {
    this.stage = world.stageNamed("service");
  }

  public void define(final String userId, final ProfileData profileData) {
    stage.actorOf(Profile.class, stage.world().addressFactory().findableBy(Integer.parseInt(userId)))
      .andFinally(profile -> {
        final Profile.State profileState = repository.profileOf(userId);
        completes().with(Response.of(Ok, headers(of(Location, profileLocation(userId))), serialized(ProfileData.from(profileState))));
        return profile;
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
