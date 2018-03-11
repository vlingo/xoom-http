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

import java.util.ArrayList;
import java.util.List;

import io.vlingo.http.Response;
import io.vlingo.http.Version;
import io.vlingo.http.resource.ResourceHandler;
import io.vlingo.http.sample.user.model.Contact;
import io.vlingo.http.sample.user.model.Name;
import io.vlingo.http.sample.user.model.User;
import io.vlingo.http.sample.user.model.UserRepository;

public class UserResource extends ResourceHandler {
  private final UserRepository repository = UserRepository.instance();

  public UserResource() { }

  public void register(final UserData userData) {
    final User user =
            User.from(
                    Name.from(userData.nameData.given, userData.nameData.family),
                    Contact.from(userData.contactData.emailAddress, userData.contactData.telephoneNumber));
    
    repository.save(user);
    
    completes().with(Response.of(Version.Http1_1, Created, headers(of(Location, userLocation(user.id))), serialized(UserData.from(user))));
  }

  public void changeContact(final String userId, final ContactData contactData) {
    final User user = repository.userOf(userId);
    if (user.doesNotExist()) {
      completes().with(Response.of(Version.Http1_1, NotFound, userLocation(userId)));
      return;
    }
    
    final User changedUser = user.withContact(new Contact(contactData.emailAddress, contactData.telephoneNumber));
    
    repository.save(changedUser);
    
    completes().with(Response.of(Version.Http1_1, Ok, serialized(UserData.from(changedUser))));
  }

  public void changeName(final String userId, final NameData nameData) {
    final User user = repository.userOf(userId);
    if (user.doesNotExist()) {
      completes().with(Response.of(Version.Http1_1, NotFound, userLocation(userId)));
      return;
    }
    
    final User changedUser = user.withName(new Name(nameData.given, nameData.family));
    
    repository.save(changedUser);
    
    completes().with(Response.of(Version.Http1_1, Ok, serialized(UserData.from(changedUser))));
  }

  public void queryUser(final String userId) {
    final User user = repository.userOf(userId);
    if (user.doesNotExist()) {
      completes().with(Response.of(Version.Http1_1, NotFound, userLocation(userId)));
    } else {
      completes().with(Response.of(Version.Http1_1, Ok, serialized(UserData.from(user))));
    }
  }

  public void queryUsers() {
    final List<UserData> users = new ArrayList<>();
    for (final User user : repository.users()) {
      users.add(UserData.from(user));
    }
    completes().with(Response.of(Version.Http1_1, Ok, serialized(users)));
  }

  private String userLocation(final String userId) {
    return "/users/" + userId;
  }
}
