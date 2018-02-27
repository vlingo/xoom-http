// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Loader {

  private static final String propertiesFile = "/vlingo-http.properties";
  private static final String resourceNamePrefix = "resource.name.";

  public static Map<String,Resource<?>> loadResources() {
    final Properties properties = loadProperties();

    final Map<String,Resource<?>> namedResources = new HashMap<>();
    
    for (String resource : findResources(properties)) {
      final Resource<?> loaded = loadResource(properties, resource);
      
      namedResources.put(loaded.name, loaded);
    }
    
    return namedResources;
  }

  private static Set<String> findResources(final Properties properties) {
    final Set<String> resource = new HashSet<String>();

    for (Enumeration<?> e = properties.keys(); e.hasMoreElements(); ) {
      final String key = (String) e.nextElement();
      if (key.startsWith(resourceNamePrefix)) {
        resource.add(key);
      }
    }

    return resource;
  }

  private static Properties loadProperties() {
    final Properties properties = new Properties();

    try {
      properties.load(Loader.class.getResourceAsStream(propertiesFile));
    } catch (IOException e) {
      throw new IllegalStateException("Must provide properties file on classpath: " + propertiesFile);
    }

    return properties;
  }

  private static Resource<?> loadResource(final Properties properties, final String resourceNameKey) {
    final String resourceName = resourceNameKey.substring(resourceNamePrefix.length());
    final String[] resourceActionNames = actionNamesFrom(properties.getProperty(resourceNameKey), resourceNameKey);
    final String dispatcherKey = "resource." + resourceName + ".dispatcher";
    final String dispatcherClassname = properties.getProperty(dispatcherKey);
    final String resourceHandlerKey = "resource." + resourceName + ".handler";
    final String resourceHandlerClassname = properties.getProperty(resourceHandlerKey);
    final String handlerPoolKey = "resource." + resourceName + ".pool";
    final int maybeHandlerPoolSize = Integer.parseInt(properties.getProperty(handlerPoolKey, "1"));
    final int handlerPoolSize = maybeHandlerPoolSize <= 0 ? 1 : maybeHandlerPoolSize;
    final String disallowPathParametersWithSlashKey = "resource." + resourceName + ".disallowPathParametersWithSlash";
    final boolean disallowPathParametersWithSlash = Boolean.parseBoolean(properties.getProperty(disallowPathParametersWithSlashKey, "true"));
    
    final List<Action> resourceActions = resourceActionsOf(properties, resourceName, resourceActionNames, disallowPathParametersWithSlash);
    
    final Object[] ctorParams = new Object[] { resourceName, resourceHandlerClassname, handlerPoolSize, resourceActions };
    
    return resourceDispatcher(dispatcherClassname, ctorParams);
  }

  private static Resource<?> resourceDispatcher(final String dispatcherClassname, final Object[] params) {
    try {
      final Class<?> resourceDispatcherClass = Class.forName(dispatcherClassname);
      for (final Constructor<?> ctor : resourceDispatcherClass.getConstructors()) {
        if (ctor.getParameterCount() == params.length) {
          final Resource<?> resourecDispatcher = (Resource<?>) ctor.newInstance(params);
          return resourecDispatcher;
        }
      }
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException("Resource dispatcher class not found: " + dispatcherClassname);
    } catch (Exception e) {
      // fall through
    }
    throw new IllegalStateException("No constructor matches the required number of parameters: " + dispatcherClassname);
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
          final Properties properties,
          final String resourceName,
          final String[] resourceActionNames,
          final boolean disallowPathParametersWithSlash) {
    final List<Action> resourceActions = new ArrayList<>(resourceActionNames.length);
    
    for (final String actionName : resourceActionNames) {
      final String keyPrefix = "action." + resourceName + "." + actionName + ".";
      
      final int actionId = resourceActions.size();
      final String method =  properties.getProperty(keyPrefix + "method", null);
      final String uri = properties.getProperty(keyPrefix + "uri", null);
      final String to = properties.getProperty(keyPrefix + "to", null);
      final String mapper = properties.getProperty(keyPrefix + "mapper", null);
      
      resourceActions.add(new Action(actionId, method, uri, to, mapper, disallowPathParametersWithSlash));
    }
    
    return resourceActions;
  }
}
