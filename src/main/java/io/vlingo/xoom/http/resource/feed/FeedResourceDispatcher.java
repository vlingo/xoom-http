// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource.feed;

import java.util.List;
import java.util.function.Consumer;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.http.Context;
import io.vlingo.xoom.http.resource.Action;
import io.vlingo.xoom.http.resource.Action.MappedParameters;
import io.vlingo.xoom.http.resource.ConfigurationResource;
import io.vlingo.xoom.http.resource.ResourceHandler;
import io.vlingo.xoom.http.resource.sse.SseStreamResource;

public class FeedResourceDispatcher extends ConfigurationResource<SseStreamResource> {

  public FeedResourceDispatcher(
          final String name,
          final Class<? extends ResourceHandler> resourceHandlerClass,
          final int handlerPoolSize,
          final List<Action> actions) {
    super(name, resourceHandlerClass, handlerPoolSize, actions);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void dispatchToHandlerWith(final Context context, final MappedParameters mappedParameters) {
    Consumer<FeedResource> consumer = null;

    try {
      switch (mappedParameters.actionId) {
      case 0: // GET /feeds/{feedName}/{feedItemId} feed(String feedName, String feedProductId, Class<? extends Actor> feedProducerClass, int feedProductElements)
        consumer = (handler) -> handler.feed((String) mappedParameters.mapped.get(0).value, (String) mappedParameters.mapped.get(1).value, (Class<? extends Actor>) mappedParameters.mapped.get(2).value, (int) mappedParameters.mapped.get(3).value);
        pooledHandler().handleFor(context, consumer);
        break;
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Action mismatch: Request: " + context.request + "Parameters: " + mappedParameters);
    }
  }
}
