// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
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
import static io.vlingo.http.resource.ResourceBuilder.get;
import static io.vlingo.http.resource.ResourceBuilder.put;
import static io.vlingo.http.resource.ResourceBuilder.resource;

import io.vlingo.actors.Definition;
import io.vlingo.actors.Stage;
import io.vlingo.actors.World;
import io.vlingo.common.Completes;
import io.vlingo.http.Response;
import io.vlingo.http.resource.Resource;
import io.vlingo.http.resource.ResourceHandler;
import io.vlingo.http.sample.user.model.Profile;
import io.vlingo.http.sample.user.model.ProfileActor;
import io.vlingo.http.sample.user.model.ProfileRepository;

public class ProfileResourceFluent extends ResourceHandler {
  private final ProfileRepository repository = ProfileRepository.instance();
  private final Stage stage;

  public ProfileResourceFluent(final World world) {
    this.stage = world.stageNamed("service");
  }

  public Completes<Response> define(final String userId, final ProfileData profileData) {
    return stage.actorOf(Profile.class, stage.world().addressFactory().findableBy(Integer.parseInt(userId)))
      .andThenTo(profile -> {
        final Profile.State profileState = repository.profileOf(userId);
        return Completes.withSuccess(Response.of(Ok, headers(of(Location, profileLocation(userId))), serialized(ProfileData.from(profileState))));
      })
      .otherwise(noProfile -> {
        final Profile.State profileState =
                Profile.from(
                        userId,
                        profileData.twitterAccount,
                        profileData.linkedInAccount,
                        profileData.website);

        stage().actorFor(Profile.class, Definition.has(ProfileActor.class, Definition.parameters(profileState)));

        repository.save(profileState);
        return Response.of(Created, serialized(ProfileData.from(profileState)));
      });
  }

  public Completes<Response> query(final String userId) {
    final Profile.State profileState = repository.profileOf(userId);
    if (profileState.doesNotExist()) {
      return Completes.withSuccess(Response.of(NotFound, profileLocation(userId)));
    } else {
      return Completes.withSuccess(Response.of(Ok, serialized(ProfileData.from(profileState))));
    }
  }

  @Override
  public Resource<?> routes() {
    return resource("profile resource fluent api",
      put("/users/{userId}/profile")
        .param(String.class)
        .body(ProfileData.class)
        .handle(this::define),
      get("/users/{userId}/profile")
        .param(String.class)
        .handle(this::query));
  }

  private String profileLocation(final String userId) {
    return "/users/" + userId + "/profile";
  }
}
