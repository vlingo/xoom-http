// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import io.vlingo.actors.Stage;
import io.vlingo.common.compiler.DynaClassLoader;
import io.vlingo.common.compiler.DynaCompiler;
import io.vlingo.common.compiler.DynaCompiler.Input;
import io.vlingo.http.Method;
import io.vlingo.http.resource.Action.MatchResults;
import io.vlingo.http.resource.ResourceDispatcherGenerator.Result;

public abstract class ConfigurationResource<T> extends Resource<T> {
  static final String DispatcherSuffix = "Dispatcher";

  private static DynaClassLoader classLoader = new DynaClassLoader(ConfigurationResource.class.getClassLoader());
  private static final DynaCompiler dynaCompiler = new DynaCompiler();

  public final Class<? extends ResourceHandler> resourceHandlerClass;
  final List<Action> actions;

  public static ConfigurationResource<?> defining(
          final String resourceName,
          final Class<? extends ResourceHandler> resourceHandlerClass,
          final int handlerPoolSize,
          final List<Action> actions) {
    return newResourceFor(resourceName, resourceHandlerClass, handlerPoolSize, actions);
  }

  @SuppressWarnings("unchecked")
  static ConfigurationResource<?> newResourceFor(
          final String resourceName,
          final Class<? extends ResourceHandler> resourceHandlerClass,
          final int handlerPoolSize,
          final List<Action> actions) {

    assertSaneActions(actions);

    try {
      final String targetClassname = resourceHandlerClass.getName() + DispatcherSuffix;

      Class<ConfigurationResource<?>> resourceClass = null;
      try {
        // this check is done primarily for testing to prevent duplicate class type in class loader
        resourceClass = (Class<ConfigurationResource<?>>) Class.forName(targetClassname);
      } catch (Exception e) {
        resourceClass = tryGenerateCompile(resourceHandlerClass, targetClassname, actions);
      }

      final Object[] ctorParams = new Object[] { resourceName, resourceHandlerClass, handlerPoolSize, actions };
      for (final Constructor<?> ctor : resourceClass.getConstructors()) {
        if (ctor.getParameterCount() == ctorParams.length) {
          final ConfigurationResource<?> resourceDispatcher = (ConfigurationResource<?>) ctor.newInstance(ctorParams);
          return resourceDispatcher;
        }
      }
      return resourceClass.newInstance();
    } catch (Exception e) {
      throw new IllegalArgumentException("Cannot create a resource from resource handler " + resourceHandlerClass.getName() + " because: " + e.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  static Class<? extends ResourceHandler> newResourceHandlerClassFor(final String resourceHandlerClassname) {
    try {
      final Class<? extends ResourceHandler> resourceHandlerClass = (Class<? extends ResourceHandler>) Class.forName(resourceHandlerClassname);
      confirmResourceHandler(resourceHandlerClass);
      return resourceHandlerClass;
    } catch (Exception e) {
      throw new IllegalArgumentException("The resource handler class " + resourceHandlerClassname + " cannot be loaded because: " + e.getMessage());
    }
  }

  private static void assertSaneActions(final List<Action> actions) {
    int expectedId = 0;
    for (final Action action : actions) {
      if (action.id != expectedId) {
        throw new IllegalArgumentException(
                "Action id in conflict with expected ordering: expected id: " +
                expectedId +
                " Action is: " +
                action);
      }
      ++expectedId;
    }
  }

  private static void confirmResourceHandler(Class<?> resourceHandlerClass) {
    Class<?> superclass = resourceHandlerClass.getSuperclass();
    while (superclass != null) {
      if (superclass == ResourceHandler.class) {
        return;
      }
      superclass = superclass.getSuperclass();
    }
    throw new IllegalStateException("ConfigurationResource handler class must extends ResourceHandler: " + resourceHandlerClass.getName());
  }

  private static Class<ConfigurationResource<?>> tryGenerateCompile(
          final Class<? extends ResourceHandler> resourceHandlerClass,
          final String targetClassname,
          final List<Action> actions) {
    try (final ResourceDispatcherGenerator generator = ResourceDispatcherGenerator.forMain(actions, true)) {
      return tryGenerateCompile(resourceHandlerClass, generator, targetClassname);
    } catch (Exception emain) {
      try (final ResourceDispatcherGenerator generator = ResourceDispatcherGenerator.forTest(actions, true)) {
        return tryGenerateCompile(resourceHandlerClass, generator, targetClassname);
      } catch (Exception etest) {
        etest.printStackTrace();
        throw new IllegalArgumentException("ConfigurationResource dispatcher for " + resourceHandlerClass.getName() + " not created for main or test because: " + etest.getMessage(), etest);
      }
    }
  }

  private static Class<ConfigurationResource<?>> tryGenerateCompile(
          final Class<? extends ResourceHandler> resourceHandlerClass,
          final ResourceDispatcherGenerator generator,
          final String targetClassname) {
    try {
      final Result result = generator.generateFor(resourceHandlerClass.getName());
      final Input input = new Input(resourceHandlerClass, targetClassname, result.source, result.sourceFile, classLoader, generator.type(), true);
      final Class<ConfigurationResource<?>> resourceDispatcherClass = dynaCompiler.compile(input);
      return resourceDispatcherClass;
    } catch (Exception e) {
      throw new IllegalArgumentException("ConfigurationResource instance with dispatcher for " + resourceHandlerClass.getName() + " not created because: " + e.getMessage(), e);
    }
  }

  MatchResults matchWith(final Method method, final URI uri) {
    for (final Action action : actions) {
      final MatchResults matchResults = action.matchWith(method, uri);
      if (matchResults.isMatched()) {
        return matchResults;
      }
    }
    return Action.unmatchedResults;
  }

  protected ConfigurationResource(
          final String name,
          final Class<? extends ResourceHandler> resourceHandlerClass,
          final int handlerPoolSize,
          final List<Action> actions) {
      super(name, handlerPoolSize);
    this.resourceHandlerClass = resourceHandlerClass;
    this.actions = Collections.unmodifiableList(actions);
  }

  protected ResourceHandler resourceHandlerInstance(final Stage stage) {
    try {
      for (final Constructor<?> ctor : resourceHandlerClass.getConstructors()) {
        if (ctor.getParameterCount() == 1) {
          return (ResourceHandler) ctor.newInstance(new Object[] { stage.world() } );
        }
      }
      return (ResourceHandler) resourceHandlerClass.newInstance();
    } catch (Exception e) {
      throw new IllegalArgumentException("The instance for resource handler '" + resourceHandlerClass.getName() + "' cannot be created.");
    }
  }
}
