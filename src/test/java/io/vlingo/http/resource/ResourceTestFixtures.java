// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import static io.vlingo.common.serialization.JsonSerialization.serialized;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;

import io.vlingo.actors.World;
import io.vlingo.http.sample.user.ContactData;
import io.vlingo.http.sample.user.NameData;
import io.vlingo.http.sample.user.UserData;
import io.vlingo.http.sample.user.model.UserRepository;
import io.vlingo.wire.message.ByteBufferAllocator;
import io.vlingo.wire.message.Converters;

public abstract class ResourceTestFixtures {
  public static final String WORLD_NAME = "resource-test";
  protected Action actionPostUser;
  protected Action actionPatchUserContact;
  protected Action actionPatchUserName;
  protected Action actionGetUser;
  protected Action actionGetUsers;
  protected Action actionGetUserError;
  protected Action actionPutUser;

  protected ConfigurationResource<?> resource;
  protected Class<? extends ResourceHandler> resourceHandlerClass;
  protected Resources resources;
  protected Dispatcher dispatcher;
  protected World world;

  protected final UserData johnDoeUserData =
          UserData.from(
                  NameData.from("John", "Doe"),
                  ContactData.from("john.doe@vlingo.io", "+1 212-555-1212"));

  protected final String johnDoeUserSerialized = serialized(johnDoeUserData);

  protected final UserData janeDoeUserData =
          UserData.from(
                  NameData.from("Jane", "Doe"),
                  ContactData.from("jane.doe@vlingo.io", "+1 212-555-1212"));

  protected final String janeDoeUserSerialized = serialized(janeDoeUserData);

  protected final String postJohnDoeUserMessage =
          "POST /users HTTP/1.1\nHost: vlingo.io\nContent-Length: " + johnDoeUserSerialized.length() + "\n\n" + johnDoeUserSerialized;

  protected final String postJaneDoeUserMessage =
          "POST /users HTTP/1.1\nHost: vlingo.io\nContent-Length: " + janeDoeUserSerialized.length() + "\n\n" + janeDoeUserSerialized;

  private final ByteBuffer buffer = ByteBufferAllocator.allocate(65535);

  private int uniqueId = 1;

  protected ByteBuffer toByteBuffer(final String requestContent) {
    buffer.clear();
    buffer.put(Converters.textToBytes(requestContent));
    buffer.flip();
    return buffer;
  }

  protected String createdResponse(final String body) {
    return "HTTP/1.1 201 CREATED\nContent-Length: " + body.length() + "\n\n" + body;
  }

  protected String postRequestCloseFollowing(final String body) {
    return "POST /users HTTP/1.1\nHost: vlingo.io\nContent-Length: " + body.length() + "\nConnection: close" + "\n\n" + body;
  }

  protected String postRequest(final String body) {
    return "POST /users HTTP/1.1\nHost: vlingo.io\nConnection: keep-alive\nContent-Length: " + body.length() + "\n\n" + body;
  }

  protected String putRequest(final String userId, final String body) {
    return "PUT /users/" + userId + " HTTP/1.1\nHost: vlingo.io\nConnection: keep-alive\nContent-Length: " + body.length() + "\n\n" + body;
  }

  protected String getExceptionRequest(final String userId) {
    return "GET /users/" + userId + "/error HTTP/1.1\nHost: vlingo.io\nConnection: keep-alive\n\n";
  }

  protected String janeDoeCreated() {
    return createdResponse(janeDoeUserSerialized);
  }

  protected String uniqueJaneDoe() {
    final UserData unique =
            UserData.from(
                    "" + uniqueId,
                    NameData.from("Jane", "Doe"),
                    ContactData.from("jane.doe@vlingo.io", "+1 212-555-1212"));

    ++uniqueId;

    final String serialized = serialized(unique);

    return serialized;
  }

  protected String uniqueJaneDoePostCreated() {
    return createdResponse(uniqueJaneDoe());
  }

  protected String uniqueJaneDoePostRequest() {
    return postRequest(uniqueJaneDoe());
  }

  protected String uniqueJohnDoe() {
    String id = "" + uniqueId;
    if (id.length() == 1) id = "00" + id;
    if (id.length() == 2) id = "0" + id;
    final UserData unique =
            UserData.from(
                    id, //"" + uniqueId,
                    NameData.from("John", "Doe"),
                    ContactData.from("john.doe@vlingo.io", "+1 212-555-1212"));

    ++uniqueId;

    final String serialized = serialized(unique);

    return serialized;
  }

  protected String johnDoeCreated() {
    return createdResponse(johnDoeUserSerialized);
  }

  protected String uniqueJohnDoePostCreated() {
    return createdResponse(uniqueJohnDoe());
  }

  protected String uniqueJohnDoePostRequest() {
    return postRequest(uniqueJohnDoe());
  }

  @Before
  public void setUp() throws Exception {
    world = World.start(WORLD_NAME);

    actionPostUser = new Action(0, "POST", "/users", "register(body:io.vlingo.http.sample.user.UserData userData)", null);
    actionPatchUserContact = new Action(1, "PATCH", "/users/{userId}/contact", "changeContact(String userId, body:io.vlingo.http.sample.user.ContactData contactData)", null);
    actionPatchUserName = new Action(2, "PATCH", "/users/{userId}/name", "changeName(String userId, body:io.vlingo.http.sample.user.NameData nameData)", null);
    actionGetUser = new Action(3, "GET", "/users/{userId}", "queryUser(String userId)", null);
    actionGetUsers = new Action(4, "GET", "/users", "queryUsers()", null);
    actionGetUserError = new Action(5, "GET", "/users/{userId}/error", "queryUserError(String userId)", null);
    actionPutUser = new Action(6, "PUT", "/users/{userId}", "changeUser(String userId, body:io.vlingo.http.sample.user.UserData userData)", null);


    final List<Action> actions =
            Arrays.asList(
                    actionPostUser,
                    actionPatchUserContact,
                    actionPatchUserName,
                    actionGetUser,
                    actionGetUsers,
                    actionGetUserError,
                    actionPutUser);

    resourceHandlerClass = ConfigurationResource.newResourceHandlerClassFor("io.vlingo.http.sample.user.UserResource");

    resource = ConfigurationResource.newResourceFor("user", resourceHandlerClass, 7, actions);

    resource.allocateHandlerPool(world.stage());

    final Map<String, Resource<?>> oneResource = new HashMap<>(1);

    oneResource.put(resource.name, resource);

    resources = new Resources(oneResource);
    dispatcher = new TestDispatcher(resources, world.defaultLogger());
  }

  @After
  public void tearDown() throws InterruptedException {
    //wait for the tearDown overridden implementations to end
    Thread.sleep(200);

    world.terminate();

    UserRepository.reset();
  }
}
