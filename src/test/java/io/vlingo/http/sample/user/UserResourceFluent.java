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
import static io.vlingo.http.resource.ResourceBuilder.get;
import static io.vlingo.http.resource.ResourceBuilder.patch;
import static io.vlingo.http.resource.ResourceBuilder.post;
import static io.vlingo.http.resource.ResourceBuilder.resource;

import java.util.ArrayList;
import java.util.List;

import io.vlingo.actors.Address;
import io.vlingo.actors.Definition;
import io.vlingo.actors.Stage;
import io.vlingo.actors.World;
import io.vlingo.common.Completes;
import io.vlingo.http.Response;
import io.vlingo.http.resource.Resource;
import io.vlingo.http.resource.ResourceHandler;
import io.vlingo.http.resource.ServerBootstrap;
import io.vlingo.http.sample.user.model.Contact;
import io.vlingo.http.sample.user.model.Name;
import io.vlingo.http.sample.user.model.User;
import io.vlingo.http.sample.user.model.UserActor;
import io.vlingo.http.sample.user.model.UserRepository;

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