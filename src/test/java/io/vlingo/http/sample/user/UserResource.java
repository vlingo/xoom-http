// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.sample.user;

import io.vlingo.actors.*;
import io.vlingo.http.Header;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseHeader;
import io.vlingo.http.resource.ResourceHandler;
import io.vlingo.http.sample.user.model.*;

import java.util.ArrayList;
import java.util.List;

import static io.vlingo.common.serialization.JsonSerialization.serialized;
import static io.vlingo.http.Response.Status.*;
import static io.vlingo.http.ResponseHeader.*;

public class UserResource extends ResourceHandler {
  private final AddressFactory addressFactory;
  private final UserRepository repository = UserRepository.instance();
  private final Stage stage;

  public UserResource(final World world) {
    this.stage = world.stageNamed("service");
    this.addressFactory = this.stage.addressFactory();
  }

  public void register(final UserData userData) {
    final Address userAddress = addressFactory.uniquePrefixedWith("u-");
    final User.State userState =
            User.from(
                    userAddress.idString(),
                    Name.from(userData.nameData.given, userData.nameData.family),
                    Contact.from(userData.contactData.emailAddress, userData.contactData.telephoneNumber));

    stage.actorFor(User.class, Definition.has(UserActor.class, Definition.parameters(userState)), userAddress);

    repository.save(userState);

    completes().with(Response.of(Created, headers(of(Location, userLocation(userState.id))), serialized(UserData.from(userState))));
  }

  public void changeUser(final String userId, UserData userData) {
    if (userId.endsWith("123")) {
      completes().with(Response.of(PermanentRedirect, Header.Headers.of(ResponseHeader.of("Location", "/app/"))));
    } else {
      completes().with(Response.of(Ok));
    }
  }

  public void changeContact(final String userId, final ContactData contactData) {
    stage.actorOf(User.class, addressFactory.from(userId))
      .andThenTo(user -> user.withContact(new Contact(contactData.emailAddress, contactData.telephoneNumber)))
      .otherwiseConsume(noUser -> completes().with(Response.of(NotFound, userLocation(userId))))
      .andFinallyConsume(userState -> Response.of(Ok, serialized(UserData.from(userState))));
  }

  public void changeName(final String userId, final NameData nameData) {
    stage.actorOf(User.class, addressFactory.from(userId))
      .andThenTo(user -> user.withName(new Name(nameData.given, nameData.family)))
      .otherwiseConsume(noUser -> completes().with(Response.of(NotFound, userLocation(userId))))
      .andFinallyConsume(userState -> {
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
