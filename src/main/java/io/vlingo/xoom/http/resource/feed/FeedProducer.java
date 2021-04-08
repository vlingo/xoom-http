// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource.feed;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.Stage;

/**
 * Produce a feed item for a named feed.
 */
public interface FeedProducer {
  /**
   * Answer a new {@code FeedProducer}.
   * @param stage the Stage in which the FeedProducer is created
   * @param feedProducerClass the {@code Class<? extends Actor>}
   * @return FeedProducer
   */
  static FeedProducer using(final Stage stage, final Class<? extends Actor> feedProducerClass) {
    return stage.actorFor(FeedProducer.class, feedProducerClass);
  }

  /**
   * Produce the feed to fulfill the {@code request}.
   * @param request the FeedProductRequest holding request information
   */
  void produceFeedFor(final FeedProductRequest request);
}
