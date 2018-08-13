// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Loader {

  private static final String resourceNamePrefix = "resource.name.";

  public static Resources loadResources(final java.util.Properties properties) {
    final Map<String, Resource<?>> namedResources = new HashMap<>();
    
    for (String resource : findResources(properties)) {
      final ConfigurationResource<?> loaded = loadResource(properties, resource);
      
      namedResources.put(loaded.name, loaded);
    }
    
    return new Resources(namedResources);
  }

  private static Set<String> findResources(final java.util.Properties properties) {
    final Set<String> resource = new HashSet<String>();

    for (Enumeration<?> e = properties.keys(); e.hasMoreElements(); ) {
      final String key = (String) e.nextElement();
      if (key.startsWith(resourceNamePrefix)) {
        resource.add(key);
      }
    }

    return resource;
  }

  private static ConfigurationResource<?> loadResource(final java.util.Properties properties, final String resourceNameKey) {
    final String resourceName = resourceNameKey.substring(resourceNamePrefix.length());
    final String[] resourceActionNames = actionNamesFrom(properties.getProperty(resourceNameKey), resourceNameKey);
    final String resourceHandlerKey = "resource." + resourceName + ".handler";
    final String resourceHandlerClassname = properties.getProperty(resourceHandlerKey);
    final String handlerPoolKey = "resource." + resourceName + ".pool";
    final int maybeHandlerPoolSize = Integer.parseInt(properties.getProperty(handlerPoolKey, "1"));
    final int handlerPoolSize = maybeHandlerPoolSize <= 0 ? 1 : maybeHandlerPoolSize;
    final String disallowPathParametersWithSlashKey = "resource." + resourceName + ".disallowPathParametersWithSlash";
    final boolean disallowPathParametersWithSlash = Boolean.parseBoolean(properties.getProperty(disallowPathParametersWithSlashKey, "true"));
    
    try {
      final List<Action> resourceActions = resourceActionsOf(properties, resourceName, resourceActionNames, disallowPathParametersWithSlash);
      
      final Class<? extends ResourceHandler> resourceHandlerClass = ConfigurationResource.newResourceHandlerClassFor(resourceHandlerClassname);
  
      return resourceFor(resourceName, resourceHandlerClass, handlerPoolSize, resourceActions);
    } catch (Exception e) {
      System.out.println("vlingo/http: Failed to load resource: " + resourceName + " because: " + e.getMessage());
      throw e;
    }
  }

  private static ConfigurationResource<?> resourceFor(
          final String resourceName,
          final Class<? extends ResourceHandler> resourceHandlerClass,
          final int handlerPoolSize,
          final List<Action> resourceActions) {
    try {
      final ConfigurationResource<?> resource = ConfigurationResource.newResourceFor(resourceName, resourceHandlerClass, handlerPoolSize, resourceActions);
      return resource;
    } catch (Exception e) {
      throw new IllegalStateException("ConfigurationResource cannot be created for: " + resourceHandlerClass.getName());
    }
  }

  private static String[] actionNamesFrom(final String actionNamesProperty, final String key) {
    final int open = actionNamesProperty.indexOf("[");
    final int close = actionNamesProperty.indexOf("]");
    
    if (open == -1 || close == -1) {
      throw new IllegalStateException("Cannot load action names for resource: " + key);
    }
    
    final String[] actionNames = actionNamesProperty.substring(open + 1, close).trim().split(",\\s?");
    
    if (actionNames.length == 0) {
      throw new IllegalStateException("Cannot load action names for resource: " + key);
    }
    
    return actionNames;
  }

  private static List<Action> resourceActionsOf(
          final java.util.Properties properties,
          final String resourceName,
          final String[] resourceActionNames,
          final boolean disallowPathParametersWithSlash) {
    final List<Action> resourceActions = new ArrayList<>(resourceActionNames.length);
    
    for (final String actionName : resourceActionNames) {
      try {
        final String keyPrefix = "action." + resourceName + "." + actionName + ".";
        
        final int actionId = resourceActions.size();
        final String method =  properties.getProperty(keyPrefix + "method", null);
        final String uri = properties.getProperty(keyPrefix + "uri", null);
        final String to = properties.getProperty(keyPrefix + "to", null);
        final String mapper = properties.getProperty(keyPrefix + "mapper", null);
        
        resourceActions.add(new Action(actionId, method, uri, to, mapper, disallowPathParametersWithSlash));
      } catch (Exception e) {
        System.out.println("vlingo/http: Failed to load resource: " + resourceName + " action:" + actionName + " because: " + e.getMessage());
        throw e;
      }
    }
    
    return resourceActions;
  }
}
