// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.feed;

import static io.vlingo.http.Response.Status.NotFound;

import java.util.HashMap;
import java.util.Map;

import io.vlingo.actors.Actor;
import io.vlingo.actors.World;
import io.vlingo.http.Response;
import io.vlingo.http.resource.ResourceHandler;

/**
 * Standard reusable resource for serving feeds.
 */
public class FeedResource extends ResourceHandler {
  private Map<String,FeedProducer> producers;
  private final World world;

  /**
   * Construct my default state.
   * @param world the World
   */
  public FeedResource(final World world) {
    this.world = world;
    this.producers = new HashMap<>(2);
  }

  /**
   * Feed the resource identified by {@code name} and {@code feedItemId}.
   * @param feedName the String name of the feed to serve
   * @param feedProductId the String identity of the feed product to serve
   * @param feedProducerClass the {@code Class<? extends Actor>} of FeedProducer
   * @param feedProductElements the int maximum number of elements in the product
   */
  public void feed(
          final String feedName,
          final String feedProductId,
          final Class<? extends Actor> feedProducerClass,
          final int feedProductElements) {

    final FeedProducer producer = feedProducer(feedName, feedProducerClass);
    if (producer == null) {
      completes().with(Response.of(NotFound, "Feed '" + feedName + "' does not exist."));
    } else {
      producer.produceFeedFor(new FeedProductRequest(context(), feedName, feedProductId, feedProductElements));
    }
  }

  private FeedProducer feedProducer(final String feedName, final Class<? extends Actor> feedProducerClass) {
    FeedProducer producer = producers.get(feedName);
    if (producer == null) {
      producer = FeedProducer.using(world.stage(), feedProducerClass);
      producers.put(feedName, producer);
    }
    return producer;
  }
}
