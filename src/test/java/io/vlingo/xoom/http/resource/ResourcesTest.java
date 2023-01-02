// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import io.vlingo.xoom.http.sample.user.ProfileResource;
import io.vlingo.xoom.http.sample.user.UserResource;

public class ResourcesTest {
  private final Resources resources = Loader.loadResources(Properties.loadProperties());
  
  @Test
  public void testLoadResources() {
    final ConfigurationResource<?> user = (ConfigurationResource<?>) resources.resourceOf("user");
    
    assertNotNull(user);
    assertEquals(user.name, "user");
    assertNotNull(user.resourceHandlerClass);
    assertEquals("io.vlingo.xoom.http.sample.user.UserResource", user.resourceHandlerClass.getName());
    assertEquals(10, user.handlerPoolSize);
    
    int countUserActions = 0;
    
    for (Action action : user.actions) {
      ++countUserActions;

      assertTrue(action.method.isPOST() || action.method.isPATCH() || action.method.isPUT() || action.method.isGET());
      
      assertNotNull(action.uri);
      assertNotNull(action.to);
      assertNotNull(action.mapper);
    }
    
    assertEquals(7, countUserActions);
    
    final ConfigurationResource<?> profile = (ConfigurationResource<?>) resources.resourceOf("profile");
    
    assertNotNull(profile);
    assertEquals(profile.name, "profile");
    assertNotNull(profile.resourceHandlerClass);
    assertEquals("io.vlingo.xoom.http.sample.user.ProfileResource", profile.resourceHandlerClass.getName());
    assertEquals(5, profile.handlerPoolSize);
    
    int countProfileActions = 0;
    
    for (Action action : profile.actions) {
      ++countProfileActions;
      
      assertTrue(action.method.isPUT() || action.method.isGET());
      
      assertNotNull(action.uri);
      assertNotNull(action.to);
      assertNotNull(action.mapper);
    }
    
    assertEquals(2, countProfileActions);
  }
  
  @Test
  public void testLoadSseResources() {
    final ConfigurationResource<?> allStream = (ConfigurationResource<?>) resources.resourceOf("all");
    
    assertNotNull(allStream);
    assertEquals(allStream.name, "all");
    assertNotNull(allStream.resourceHandlerClass);
    assertEquals("io.vlingo.xoom.http.resource.sse.SseStreamResource", allStream.resourceHandlerClass.getName());
    assertEquals(10, allStream.handlerPoolSize);
    
    assertEquals(2, allStream.actions.size());
    assertTrue(allStream.actions.get(0).method.isGET());
    assertNotNull(allStream.actions.get(0).uri);
    assertEquals("/eventstreams/{streamName}", allStream.actions.get(0).uri);
    assertNotNull(allStream.actions.get(0).to);
    assertNotNull(allStream.actions.get(0).mapper);
    assertTrue(allStream.actions.get(1).method.isDELETE());
    assertNotNull(allStream.actions.get(1).uri);
    assertEquals("/eventstreams/{streamName}/{id}", allStream.actions.get(1).uri);
    assertNotNull(allStream.actions.get(1).to);
    assertNotNull(allStream.actions.get(1).mapper);
  }

  @Test
  public void testThatResourcesBuildFluently() {
    final Resources resources =
            Resources
              .are(ConfigurationResource.defining("user", UserResource.class, 10,
                      Actions.canBe("POST", "/users", "register(body:io.vlingo.xoom.http.sample.user.UserData userData)")
                              .also("PATCH", "/users/{userId}/contact", "changeContact(String userId, body:io.vlingo.xoom.http.sample.user.ContactData contactData)")
                              .also("PATCH", "/users/{userId}/name", "changeName(String userId, body:io.vlingo.xoom.http.sample.user.NameData nameData)")
                              .also("GET", "/users/{userId}", "queryUser(String userId)")
                              .also("GET", "/users", "queryUsers()")
                              .also("PUT", "/users/{userId}", "changeUser(String userId, body:io.vlingo.xoom.http.sample.user.UserData userData)")
                              .thatsAll()),
                   ConfigurationResource.defining("profile", ProfileResource.class, 5,
                      Actions.canBe("PUT", "/users/{userId}/profile", "define(String userId, body:io.vlingo.xoom.http.sample.user.ProfileData profileData)", "io.vlingo.xoom.http.sample.user.ProfileDataMapper")
                              .also("GET", "/users/{userId}/profile", "query(String userId)", "io.vlingo.xoom.http.sample.user.ProfileDataMapper")
                              .thatsAll()));

    assertNotNull(resources.resourceOf("user"));
    assertNotNull(resources.resourceOf("profile"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testThatWrongIdSequenceBreaks() {
    final Action actionPostUser = new Action(0, "POST", "/users", "register(body:io.vlingo.xoom.http.sample.user.UserData userData)", null);
    final Action actionPatchUserContact = new Action(3, "PATCH", "/users/{userId}/contact", "changeContact(String userId, body:io.vlingo.xoom.http.sample.user.ContactData contactData)", null);

    final List<Action> actions = Arrays.asList(actionPostUser, actionPatchUserContact);

    final Class<? extends ResourceHandler> resourceHandlerClass =
            ConfigurationResource.newResourceHandlerClassFor("io.vlingo.xoom.http.sample.user.UserResource");
    
    ConfigurationResource.newResourceFor("user", resourceHandlerClass, 5, actions);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testThatWrongIdOrderBreaks() {
    final Action actionPostUser = new Action(3, "POST", "/users", "register(body:io.vlingo.xoom.http.sample.user.UserData userData)", null);
    final Action actionPatchUserContact = new Action(1, "PATCH", "/users/{userId}/contact", "changeContact(String userId, body:io.vlingo.xoom.http.sample.user.ContactData contactData)", null);

    final List<Action> actions = Arrays.asList(actionPostUser, actionPatchUserContact);

    final Class<? extends ResourceHandler> resourceHandlerClass =
            ConfigurationResource.newResourceHandlerClassFor("io.vlingo.xoom.http.sample.user.UserResource");
    
    ConfigurationResource.newResourceFor("user", resourceHandlerClass, 5, actions);
  }
}
