// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource.feed;

import static io.vlingo.xoom.http.Response.Status.Ok;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.http.Response;

public class EventsFeedProducerActor extends Actor implements FeedProducer {
  public EventsFeedProducerActor() { }

  @Override
  public void produceFeedFor(final FeedProductRequest request) {
    final StringBuilder body =
            new StringBuilder()
            .append(request.feedName)
            .append(":")
            .append(request.feedProductId)
            .append(":");

    for (int count = 1; count <= request.feedProductElements; ++count) {
      body.append(count).append("\n");
    }

    final Response response = Response.of(Ok, body.toString());
    request.context.completes.with(response);
  }
}
