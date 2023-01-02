// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource.sse;

import io.vlingo.xoom.actors.*;
import io.vlingo.xoom.common.SerializableConsumer;

public class SsePublisher__Proxy implements io.vlingo.xoom.http.resource.sse.SsePublisher {

  private static final String representationConclude0 = "conclude()";
  private static final String subscribeRepresentation1 = "subscribe(io.vlingo.xoom.http.resource.sse.SseSubscriber)";
  private static final String unsubscribeRepresentation2 = "unsubscribe(io.vlingo.xoom.http.resource.sse.SseSubscriber)";
  private static final String stopRepresentation3 = "stop()";

  private final Actor actor;
  private final Mailbox mailbox;

  public SsePublisher__Proxy(final Actor actor, final Mailbox mailbox){
    this.actor = actor;
    this.mailbox = mailbox;
  }

  @Override
  public void subscribe(io.vlingo.xoom.http.resource.sse.SseSubscriber arg0) {
    if (!actor.isStopped()) {
      final SerializableConsumer<SsePublisher> consumer = (actor) -> actor.subscribe(arg0);
      if (mailbox.isPreallocated()) { mailbox.send(actor, SsePublisher.class, consumer, null, subscribeRepresentation1); }
      else { mailbox.send(new LocalMessage<SsePublisher>(actor, SsePublisher.class, consumer, subscribeRepresentation1)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, subscribeRepresentation1));
    }
  }
  @Override
  public void unsubscribe(io.vlingo.xoom.http.resource.sse.SseSubscriber arg0) {
    if (!actor.isStopped()) {
      final SerializableConsumer<SsePublisher> consumer = (actor) -> actor.unsubscribe(arg0);
      if (mailbox.isPreallocated()) { mailbox.send(actor, SsePublisher.class, consumer, null, unsubscribeRepresentation2); }
      else { mailbox.send(new LocalMessage<SsePublisher>(actor, SsePublisher.class, consumer, unsubscribeRepresentation2)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, unsubscribeRepresentation2));
    }
  }
  @Override
  public void conclude() {
    if (!actor.isStopped()) {
      final SerializableConsumer<Stoppable> consumer = (actor) -> actor.conclude();
      if (mailbox.isPreallocated()) { mailbox.send(actor, Stoppable.class, consumer, null, representationConclude0); }
      else { mailbox.send(new LocalMessage<Stoppable>(actor, Stoppable.class, consumer, representationConclude0)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, representationConclude0));
    }
  }
  @Override
  public void stop() {
    if (!actor.isStopped()) {
      final SerializableConsumer<SsePublisher> consumer = (actor) -> actor.stop();
      if (mailbox.isPreallocated()) { mailbox.send(actor, SsePublisher.class, consumer, null, stopRepresentation3); }
      else { mailbox.send(new LocalMessage<SsePublisher>(actor, SsePublisher.class, consumer, stopRepresentation3)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, stopRepresentation3));
    }
  }
  @Override
  public boolean isStopped() {
    return actor.isStopped();
  }
}
