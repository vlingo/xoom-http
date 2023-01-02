// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import io.vlingo.xoom.actors.*;
import io.vlingo.xoom.common.SerializableConsumer;

public class Dispatcher__Proxy implements Dispatcher {

  private static final String representationConclude0 = "conclude()";
  private static final String dispatchForRepresentation1 = "dispatchFor(io.vlingo.xoom.http.Context)";
  private static final String stopRepresentation2 = "stop()";

  private final Actor actor;
  private final Mailbox mailbox;

  public Dispatcher__Proxy(final Actor actor, final Mailbox mailbox){
    this.actor = actor;
    this.mailbox = mailbox;
  }

  @Override
  public void dispatchFor(io.vlingo.xoom.http.Context arg0) {
    if (!actor.isStopped()) {
      final SerializableConsumer<Dispatcher> consumer = (actor) -> actor.dispatchFor(arg0);
      if (mailbox.isPreallocated()) { mailbox.send(actor, Dispatcher.class, consumer, null, dispatchForRepresentation1); }
      else { mailbox.send(new LocalMessage<Dispatcher>(actor, Dispatcher.class, consumer, dispatchForRepresentation1)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, dispatchForRepresentation1));
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
      final SerializableConsumer<Dispatcher> consumer = (actor) -> actor.stop();
      if (mailbox.isPreallocated()) { mailbox.send(actor, Dispatcher.class, consumer, null, stopRepresentation2); }
      else { mailbox.send(new LocalMessage<Dispatcher>(actor, Dispatcher.class, consumer, stopRepresentation2)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, stopRepresentation2));
    }
  }
  @Override
  public boolean isStopped() {
    return actor.isStopped();
  }
}
