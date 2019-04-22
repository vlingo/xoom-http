// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.util.List;
import java.util.function.Consumer;

import io.vlingo.http.Context;
import io.vlingo.http.resource.Action.MappedParameters;

public class StaticFilesResourceDispatcher extends ConfigurationResource<StaticFilesResource> {

  public StaticFilesResourceDispatcher(
          final String name,
          final Class<? extends ResourceHandler> resourceHandlerClass,
          final int handlerPoolSize,
          final List<Action> actions) {
    super(name, resourceHandlerClass, handlerPoolSize, actions);
  }

  @Override
  public void dispatchToHandlerWith(final Context context, final MappedParameters mappedParameters) {
    Consumer<StaticFilesResource> consumer = null;

    try {
      switch (mappedParameters.actionId) {
      case 0: // GET %root%{path} serveFile(String root, String paths, String contentFilePath)
        consumer = (handler) -> handler.serveFile((String) mappedParameters.mapped.get(0).value, (String) mappedParameters.mapped.get(1).value, (String) mappedParameters.mapped.get(2).value);
        pooledHandler().handleFor(context, consumer);
        break;
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Action mismatch: Request: " + context.request + "Parameters: " + mappedParameters);
    }
  }
}
