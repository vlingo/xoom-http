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

import java.util.ArrayList;
import java.util.List;

import io.vlingo.actors.Address;
import io.vlingo.actors.Definition;
import io.vlingo.actors.Stage;
import io.vlingo.actors.World;
import io.vlingo.http.Response;
import io.vlingo.http.resource.ResourceHandler;
import io.vlingo.http.sample.user.model.Contact;
import io.vlingo.http.sample.user.model.Name;
import io.vlingo.http.sample.user.model.User;
import io.vlingo.http.sample.user.model.UserActor;
import io.vlingo.http.sample.user.model.UserRepository;

public class UserResource extends ResourceHandler {
  private final UserRepository repository = UserRepository.instance();
  private final Stage stage;

  public UserResource(final World world) {
    this.stage = world.stageNamed("service");
  }

  public void register(final UserData userData) {
    final Address userAddress = stage().world().addressFactory().uniquePrefixedWith("u-");
    final User.State userState =
            User.from(
                    userAddress.idString(),
                    Name.from(userData.nameData.given, userData.nameData.family),
                    Contact.from(userData.contactData.emailAddress, userData.contactData.telephoneNumber));

    stage.actorFor(User.class, Definition.has(UserActor.class, Definition.parameters(userState)), userAddress);

    repository.save(userState);

    completes().with(Response.of(Created, headers(of(Location, userLocation(userState.id))), serialized(UserData.from(userState))));
  }

  public void changeContact(final String userId, final ContactData contactData) {
    stage.actorOf(User.class, stage().world().addressFactory().from(userId))
      .andThenTo(user -> user.withContact(new Contact(contactData.emailAddress, contactData.telephoneNumber)))
      .otherwiseConsume(noUser -> completes().with(Response.of(NotFound, userLocation(userId))))
      .andThenConsume(userState -> Response.of(Ok, serialized(UserData.from(userState))));
  }

  public void changeName(final String userId, final NameData nameData) {
    stage.actorOf(User.class, stage().world().addressFactory().from(userId))
      .andThenTo(user -> user.withName(new Name(nameData.given, nameData.family)))
      .otherwiseConsume(noUser -> completes().with(Response.of(NotFound, userLocation(userId))))
      .andThenConsume(userState -> {
            repository.save(userState);
            completes().with(Response.of(Ok, serialized(UserData.from(userState))));
      });
  }

  public void queryUser(final String userId) {
    final User.State userState = repository.userOf(userId);
    if (userState.doesNotExist()) {
      completes().with(Response.of(NotFound, userLocation(userId)));
    } else {
      completes().with(Response.of(Ok, serialized(UserData.from(userState))));
    }
  }

  public void queryUserError(String userId) {
    throw new Error("Test exception");
  }

  public void queryUsers() {
    final List<UserData> users = new ArrayList<>();
    for (final User.State userState : repository.users()) {
      users.add(UserData.from(userState));
    }
    completes().with(Response.of(Ok, serialized(users)));
  }

  private String userLocation(final String userId) {
    return "/users/" + userId;
  }
}
