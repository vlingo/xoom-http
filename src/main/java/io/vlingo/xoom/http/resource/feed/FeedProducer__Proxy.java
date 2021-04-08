// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource.feed;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.ActorProxyBase;
import io.vlingo.xoom.actors.DeadLetter;
import io.vlingo.xoom.actors.Definition.SerializationProxy;
import io.vlingo.xoom.actors.LocalMessage;
import io.vlingo.xoom.actors.Mailbox;
import io.vlingo.xoom.common.SerializableConsumer;

public class FeedProducer__Proxy extends ActorProxyBase<io.vlingo.xoom.http.resource.feed.FeedProducer> implements io.vlingo.xoom.http.resource.feed.FeedProducer {

  private static final String produceFeedForRepresentation1 = "produceFeedFor(io.vlingo.xoom.http.resource.feed.FeedProductRequest)";

  private final Actor actor;
  private final Mailbox mailbox;

  public FeedProducer__Proxy(final Actor actor, final Mailbox mailbox){
    super(io.vlingo.xoom.http.resource.feed.FeedProducer.class, SerializationProxy.from(actor.definition()), actor.address());
    this.actor = actor;
    this.mailbox = mailbox;
  }

  public FeedProducer__Proxy(){
    super();
    this.actor = null;
    this.mailbox = null;
  }

  @Override
  public void produceFeedFor(io.vlingo.xoom.http.resource.feed.FeedProductRequest arg0) {
    if (!actor.isStopped()) {
      ActorProxyBase<FeedProducer> self = this;
      final SerializableConsumer<FeedProducer> consumer = (actor) -> actor.produceFeedFor(ActorProxyBase.thunk(self, (Actor)actor, arg0));
      if (mailbox.isPreallocated()) { mailbox.send(actor, FeedProducer.class, consumer, null, produceFeedForRepresentation1); }
      else { mailbox.send(new LocalMessage<FeedProducer>(actor, FeedProducer.class, consumer, produceFeedForRepresentation1)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, produceFeedForRepresentation1));
    }
  }
}
