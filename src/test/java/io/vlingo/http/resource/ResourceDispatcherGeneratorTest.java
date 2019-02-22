// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.http.resource.ResourceDispatcherGenerator.Result;

public class ResourceDispatcherGeneratorTest {
  private Action actionPostUser;
  private Action actionPatchUserContact;
  private Action actionPatchUserName;
  private Action actionGetUser;
  private Action actionGetUsers;
  private Action actionQueryUserError;
  private List<Action> actions;
  private ConfigurationResource<?> resource;

  @Test
  public void testSourceCodeGeneration() throws Exception {
    final ResourceDispatcherGenerator generator = ResourceDispatcherGenerator.forTest(actions, false);

    final Result result = generator.generateFor(resource.resourceHandlerClass.getName());

    assertNotNull(result);
    assertNotNull(result.sourceFile);
    assertFalse(result.sourceFile.exists());
    assertNotNull(result.fullyQualifiedClassname);
    assertNotNull(result.classname);
    assertNotNull(result.source);
  }

  @Test
  public void testSourceCodeGenerationWithPersistence() throws Exception {
    final ResourceDispatcherGenerator generator = ResourceDispatcherGenerator.forTest(actions, true);

    final Result result = generator.generateFor(resource.resourceHandlerClass.getName());

    assertNotNull(result);

    assertNotNull(result);
    assertNotNull(result.sourceFile);
    assertTrue(result.sourceFile.exists());
    assertNotNull(result.fullyQualifiedClassname);
    assertNotNull(result.classname);
    assertNotNull(result.source);
  }

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() {
    actionPostUser = new Action(0, "POST", "/users", "register(body:io.vlingo.http.sample.user.UserData userData)", null, true);
    actionPatchUserContact = new Action(1, "PATCH", "/users/{userId}/contact", "changeContact(String userId, body:io.vlingo.http.sample.user.ContactData contactData)", null, true);
    actionPatchUserName = new Action(2, "PATCH", "/users/{userId}/name", "changeName(String userId, body:io.vlingo.http.sample.user.NameData nameData)", null, true);
    actionGetUser = new Action(3, "GET", "/users/{userId}", "queryUser(String userId)", null, true);
    actionGetUsers = new Action(4, "GET", "/users", "queryUsers()", null, true);
    actionQueryUserError = new Action(5, "GET", "/users/{userId}/error", "queryUserError(String userId)", null, true);

    actions =
            Arrays.asList(
                    actionPostUser,
                    actionPatchUserContact,
                    actionPatchUserName,
                    actionGetUser,
                    actionGetUsers,
                    actionQueryUserError);

    Class<? extends ResourceHandler> resourceHandlerClass = null;

    try {
      resourceHandlerClass = (Class<? extends ResourceHandler>) Class.forName("io.vlingo.http.sample.user.UserResource");
    } catch (Exception e) {
      resourceHandlerClass = ConfigurationResource.newResourceHandlerClassFor("io.vlingo.http.sample.user.UserResource");
    }

    resource = ConfigurationResource.newResourceFor("user", resourceHandlerClass, 5, actions);
  }
}
