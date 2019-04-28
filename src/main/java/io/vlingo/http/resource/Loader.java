// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import io.vlingo.actors.Actor;
import io.vlingo.actors.ActorFactory;
import io.vlingo.http.Method;
import io.vlingo.http.resource.Action.MappedParameter;
import io.vlingo.http.resource.sse.SseFeed;
import io.vlingo.http.resource.sse.SseStreamResource;

public class Loader {

  private static final String resourceNamePrefix = "resource.name.";
  private static final String ssePublisherFeedClassnameParameter = "Class<? extends Actor> feedClass";
  private static final String ssePublisherFeedDefaultId = "String feedDefaultId";
  private static final String ssePublisherFeedIntervalParameter = "int feedInterval";
  private static final String ssePublisherFeedPayloadParameter = "int feedPayload";
  private static final String ssePublisherIdPathParameter = "{id}";
  private static final String ssePublisherNamePrefix = "sse.stream.name.";
  private static final String ssePublisherNamePathParameter = "{streamName}";
  private static final String ssePublisherSubscribeTo =
          "subscribeToStream(String streamName, " +
                  ssePublisherFeedClassnameParameter + ", " +
                  ssePublisherFeedPayloadParameter + ", " +
                  ssePublisherFeedIntervalParameter + ", " +
                  ssePublisherFeedDefaultId + ")";
  private static final String ssePublisherUnsubscribeTo = "unsubscribeFromStream(String streamName, String id)";
  private static final String staticFilesResource = "static.files";
  private static final String staticFilesResourcePool = "static.files.resource.pool";
  private static final String staticFilesResourceRoot = "static.files.resource.root";
  private static final String staticFilesResourceSubPaths = "static.files.resource.subpaths";
  private static final String staticFilesResourceServeFile = "serveFile(String contentFile, String root, String validSubPaths)";
  private static final String staticFilesResourcePathParameter = "{contentFile}";

  public static Resources loadResources(final java.util.Properties properties) {
    final Map<String, Resource<?>> namedResources = new HashMap<>();

    for (String resource : findResources(properties, resourceNamePrefix)) {
      final ConfigurationResource<?> loaded = loadResource(properties, resource);

      namedResources.put(loaded.name, loaded);
    }

    namedResources.putAll(loadSseResources(properties));

    namedResources.putAll(loadStaticFilesResource(properties));

    return new Resources(namedResources);
  }

  private static Set<String> findResources(final java.util.Properties properties, final String namePrefix) {
    final Set<String> resource = new HashSet<String>();

    for (Enumeration<?> e = properties.keys(); e.hasMoreElements(); ) {
      final String key = (String) e.nextElement();
      if (key.startsWith(namePrefix)) {
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

  private static Map<String, ConfigurationResource<?>> loadSseResources(final Properties properties) {
    final Map<String, ConfigurationResource<?>> sseResourceActions = new HashMap<>();

    for (final String streamResourceName : findResources(properties, ssePublisherNamePrefix)) {
      final String streamURI = properties.getProperty(streamResourceName);
      final String resourceName = streamResourceName.substring(ssePublisherNamePrefix.length());
      final String feedClassnameKey = "sse.stream." + resourceName + ".feed.class";
      final String feedClassname = properties.getProperty(feedClassnameKey);
      final String feedPayloadKey = "sse.stream." + resourceName + ".feed.payload";
      final int maybeFeedPayload = Integer.parseInt(properties.getProperty(feedPayloadKey, "20"));
      final int feedPayload = maybeFeedPayload <= 0 ? 20 : maybeFeedPayload;
      final String feedIntervalKey = "sse.stream." + resourceName + ".feed.interval";
      final int maybeFeedInterval = Integer.parseInt(properties.getProperty(feedIntervalKey, "1000"));
      final int feedInterval = maybeFeedInterval <= 0 ? 1000 : maybeFeedInterval;
      final String feedDefaultIdKey = "sse.stream." + resourceName + ".feed.default.id";
      final String feedDefaultId = properties.getProperty(feedDefaultIdKey, "");
      final String poolKey = "sse.stream." + resourceName + ".pool";
      final int maybePoolSize = Integer.parseInt(properties.getProperty(poolKey, "1"));
      final int handlerPoolSize = maybePoolSize <= 0 ? 1 : maybePoolSize;
      final String subscribeURI = streamURI.replaceAll(resourceName, ssePublisherNamePathParameter);
      final String unsubscribeURI = subscribeURI + "/" + ssePublisherIdPathParameter;

      try {
        final Class<? extends Actor> feedClass = ActorFactory.actorClassWithProtocol(feedClassname, SseFeed.class);
        final MappedParameter mappedParameterClass = new MappedParameter("Class<? extends Actor>", feedClass);
        final MappedParameter mappedParameterPayload = new MappedParameter("int", feedPayload);
        final MappedParameter mappedParameterInterval = new MappedParameter("int", feedInterval);
        final MappedParameter mappedParameterDefaultId = new MappedParameter("String", feedDefaultId);

        final List<Action> actions = new ArrayList<>(2);
        final List<MappedParameter> additionalParameters = Arrays.asList(mappedParameterClass, mappedParameterPayload, mappedParameterInterval, mappedParameterDefaultId);
        actions.add(new Action(0, Method.GET.name, subscribeURI, ssePublisherSubscribeTo, null, true, additionalParameters));
        actions.add(new Action(1, Method.DELETE.name, unsubscribeURI, ssePublisherUnsubscribeTo, null, true));
        final ConfigurationResource<?> resource = resourceFor(resourceName, SseStreamResource.class, handlerPoolSize, actions);
        sseResourceActions.put(resourceName, resource);
      } catch (Exception e) {
        System.out.println("vlingo/http: Failed to load SSE resource: " + streamResourceName + " because: " + e.getMessage());
        e.printStackTrace();
        throw e;
      }
    }

    return sseResourceActions;
  }

  private static Map<String, ConfigurationResource<?>> loadStaticFilesResource(final Properties properties) {
    final Map<String, ConfigurationResource<?>> staticFilesResourceActions = new HashMap<>();

    final String root = properties.getProperty(staticFilesResourceRoot);

    if (root == null) {
      return staticFilesResourceActions;
    }

    final String poolSize = properties.getProperty(staticFilesResourcePool, "5");
    final String validSubPaths = properties.getProperty(staticFilesResourceSubPaths);
    final String[] actionSubPaths = actionNamesFrom(validSubPaths, staticFilesResourceSubPaths);
    Arrays.sort(actionSubPaths, new Comparator<String>() {
      @Override
      public int compare(final String path1, final String path2) {
        return path2.length() - path1.length();
      }
    });

    try {
      int resourceSequence = 0;

      for (final String actionSubPath : actionSubPaths) {
        final MappedParameter mappedParameterRoot = new MappedParameter("String", root);
        final MappedParameter mappedParameterValidSubPaths = new MappedParameter("String", validSubPaths);

        final String slash = actionSubPath.endsWith("/") ? "" : "/";
        final String resourceName = staticFilesResource + resourceSequence++;

        final List<Action> actions = new ArrayList<>(1);
        final List<MappedParameter> additionalParameters = Arrays.asList(mappedParameterRoot, mappedParameterValidSubPaths);
        actions.add(new Action(0, Method.GET.name, actionSubPath + slash + staticFilesResourcePathParameter, staticFilesResourceServeFile, null, false, additionalParameters));
        final ConfigurationResource<?> resource = resourceFor(resourceName, StaticFilesResource.class, Integer.parseInt(poolSize), actions);
        staticFilesResourceActions.put(resourceName, resource);
      }
    } catch (Exception e) {
      System.out.println("vlingo/http: Failed to load static files resource: " + staticFilesResource + " because: " + e.getMessage());
      e.printStackTrace();
      throw e;
    }

    return staticFilesResourceActions;
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
