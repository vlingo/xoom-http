// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
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
import static io.vlingo.xoom.http.resource.ResourceBuilder.get;
import static io.vlingo.xoom.http.resource.ResourceBuilder.patch;
import static io.vlingo.xoom.http.resource.ResourceBuilder.post;
import static io.vlingo.xoom.http.resource.ResourceBuilder.put;
import static io.vlingo.xoom.http.resource.ResourceBuilder.resource;

import java.util.ArrayList;
import java.util.List;

import io.vlingo.xoom.actors.Address;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Stage;
import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.resource.Resource;
import io.vlingo.xoom.http.resource.ResourceHandler;
import io.vlingo.xoom.http.resource.ServerBootstrap;
import io.vlingo.xoom.http.sample.user.model.Contact;
import io.vlingo.xoom.http.sample.user.model.Name;
import io.vlingo.xoom.http.sample.user.model.User;
import io.vlingo.xoom.http.sample.user.model.UserActor;
import io.vlingo.xoom.http.sample.user.model.UserRepository;

public class UserResourceFluent extends ResourceHandler {
  private final UserRepository repository = UserRepository.instance();
  private final Stage stage;

  public UserResourceFluent(final World world) {
    this.stage = world.stageNamed("service");
  }

  public Completes<Response> register(final UserData userData) {
    final Address userAddress = ServerBootstrap.instance.world.addressFactory().uniquePrefixedWith("u-"); // stage().world().addressFactory().uniquePrefixedWith("u-");
    final User.State userState =
            User.from(
                    userAddress.idString(),
                    Name.from(userData.nameData.given, userData.nameData.family),
                    Contact.from(userData.contactData.emailAddress, userData.contactData.telephoneNumber));

    stage.actorFor(User.class, Definition.has(UserActor.class, Definition.parameters(userState)), userAddress);

    repository.save(userState);

    return Completes.withSuccess(Response.of(Created, headers(of(Location, userLocation(userState.id))), serialized(UserData.from(userState))));
  }

  public Completes<Response> changeUser(final String userId, UserData userData) {
    System.out.println("PUT: " + userId);
    return Completes.withSuccess(Response.of(Ok));
  }

  public Completes<Response> changeContact(final String userId, final ContactData contactData) {
    return stage.actorOf(User.class, stage().world().addressFactory().from(userId))
      .andThenTo(user -> user.withContact(new Contact(contactData.emailAddress, contactData.telephoneNumber)))
      .otherwiseConsume(noUser -> completes().with(Response.of(NotFound, userLocation(userId))))
      .andThenTo(userState -> Completes.withSuccess(Response.of(Ok, serialized(UserData.from(userState)))));
  }

  public Completes<Response> changeName(final String userId, final NameData nameData) {
    return stage.actorOf(User.class, stage().world().addressFactory().from(userId))
      .andThenTo(user -> user.withName(new Name(nameData.given, nameData.family)))
      .otherwiseConsume(noUser -> completes().with(Response.of(NotFound, userLocation(userId))))
      .andThenTo(userState -> {
            repository.save(userState);
            return Completes.withSuccess(Response.of(Ok, serialized(UserData.from(userState))));
      });
  }

  public Completes<Response> queryUser(final String userId) {
    final User.State userState = repository.userOf(userId);
    if (userState.doesNotExist()) {
      return Completes.withSuccess(Response.of(NotFound, userLocation(userId)));
    } else {
      return Completes.withSuccess(Response.of(Ok, serialized(UserData.from(userState))));
    }
  }

  public void queryUserError(String userId) {
    throw new Error("Test exception");
  }

  public Completes<Response> queryUsers() {
    final List<UserData> users = new ArrayList<>();
    for (final User.State userState : repository.users()) {
      users.add(UserData.from(userState));
    }
    return Completes.withSuccess(Response.of(Ok, serialized(users)));
  }

  @Override
  public Resource<?> routes() {
    return resource("user resource fluent api",
      post("/users")
        .body(UserData.class)
        .handle(this::register),
      put("/users/{userId}")
        .param(String.class)
        .body(UserData.class)
        .handle(this::changeUser),
      patch("/users/{userId}/contact")
        .param(String.class)
        .body(ContactData.class)
        .handle(this::changeContact),
      patch("/users/{userId}/name")
        .param(String.class)
        .body(NameData.class)
        .handle(this::changeName),
      get("/users/{userId}")
        .param(String.class)
        .handle(this::queryUser),
      get("/users")
        .handle(this::queryUsers));
  }

  private String userLocation(final String userId) {
    return "/users/" + userId;
  }
}
