// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.feed;

import static io.vlingo.http.Response.Status.Ok;

import io.vlingo.actors.Actor;
import io.vlingo.http.Response;

public class EventsFeedProducerActor extends Actor implements FeedProducer {
  public EventsFeedProducerActor() { }

  @Override
  public void produceFeedFor(final FeedProductRequest request) {
    final String body = request.feedName + ":" + request.feedProductId + ":" + "1 2 3 4 5";
    final Response response = Response.of(Ok, body);
    request.context.completes.with(response);
  }
}
