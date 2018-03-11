// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import static io.vlingo.http.resource.serialization.JsonSerialization.serialized;

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
  protected Action actionPostUser;
  protected Action actionPatchUserContact;
  protected Action actionPatchUserName;
  protected Action actionGetUser;
  protected Action actionGetUsers;

  protected Resource<?> resource;
  protected Class<? extends ResourceHandler> resourceHandlerClass;
  protected Resources resources;
  protected Dispatcher dispatcher;
  protected World world;
  
  protected final UserData johnDoeUserData =
          UserData.from(
                  NameData.from("John", "Doe"),
                  ContactData.from("john.doe@vlingo.io", "+1 212-555-1212"));

  protected final String postJohnDoeUserSerialized = serialized(johnDoeUserData);

  protected final UserData janeDoeUserData =
          UserData.from(
                  NameData.from("Jane", "Doe"),
                  ContactData.from("jane.doe@vlingo.io", "+1 212-555-1212"));

  protected final String postJaneDoeUserSerialized = serialized(janeDoeUserData);

  protected final String postJohnDoeUserMessage =
          "POST /users HTTP/1.1\nHost: vlingo.io\nContent-Length: " + postJohnDoeUserSerialized.length() + "\n\n" + postJohnDoeUserSerialized;

  protected final String postJaneDoeUserMessage =
          "POST /users HTTP/1.1\nHost: vlingo.io\nContent-Length: " + postJaneDoeUserSerialized.length() + "\n\n" + postJaneDoeUserSerialized;

  private final ByteBuffer buffer = ByteBufferAllocator.allocate(1024);

  protected ByteBuffer toByteBuffer(final String requestContent) {
    buffer.clear();
    buffer.put(Converters.textToBytes(requestContent));
    buffer.flip();
    return buffer;
  }

  @Before
  public void setUp() throws Exception {
    world = World.start("resource-test");
    
    actionPostUser = new Action(0, "POST", "/users", "register(body:io.vlingo.http.sample.user.UserData userData)", null, true);
    actionPatchUserContact = new Action(1, "PATCH", "/users/{userId}/contact", "changeContact(String userId, body:io.vlingo.http.sample.user.ContactData contactData)", null, true);
    actionPatchUserName = new Action(2, "PATCH", "/users/{userId}/name", "changeName(String userId, body:io.vlingo.http.sample.user.NameData nameData)", null, true);
    actionGetUser = new Action(3, "GET", "/users/{userId}", "queryUser(String userId)", null, true);
    actionGetUsers = new Action(4, "GET", "/users", "queryUsers()", null, true);

    final List<Action> actions =
            Arrays.asList(
                    actionPostUser,
                    actionPatchUserContact,
                    actionPatchUserName,
                    actionGetUser,
                    actionGetUsers);

    resourceHandlerClass = Resource.newResourceHandlerClassFor("io.vlingo.http.sample.user.UserResource");
    
    resource = Resource.newResourceFor("user", resourceHandlerClass, 5, actions);
    
    resource.allocateHandlerPool(world.stage());
    
    final Map<String,Resource<?>> oneResource = new HashMap<>(1);
    
    oneResource.put(resource.name, resource);
    
    resources = new Resources(oneResource);
    
    dispatcher = new TestDispatcher(resources);
  }

  @After
  public void tearDown() {
    world.terminate();
    
    UserRepository.reset();
  }
}
