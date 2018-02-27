// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import static org.junit.Assert.*;

import org.junit.Test;

public class ResourcesTest {
  private final Resources resources = new Resources(Loader.loadResources());
  
  @Test
  public void testLoadResources() {
    final Resource<?> user = resources.resourceOf("user");
    
    assertNotNull(user);
    assertEquals(user.name, "user");
    assertNotNull(user.resourceHandlerClass);
    assertEquals("io.vlingo.http.sample.user.UserResource", user.resourceHandlerClass.getName());
    assertEquals(10, user.handlerPoolSize);
    
    int countUserActions = 0;
    
    for (Action action : user.actions) {
      ++countUserActions;
      
      assertTrue(action.method.isPOST() || action.method.isPATCH() || action.method.isGET());
      
      assertNotNull(action.uri);
      assertNotNull(action.to);
      assertNotNull(action.mapper);
    }
    
    assertEquals(5, countUserActions);
    
    
    final Resource<?> profile = resources.resourceOf("profile");
    
    assertNotNull(profile);
    assertEquals(profile.name, "profile");
    assertNotNull(profile.resourceHandlerClass);
    assertEquals("io.vlingo.http.sample.user.ProfileResource", profile.resourceHandlerClass.getName());
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
}
