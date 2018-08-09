// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.util.function.Consumer;

import io.vlingo.actors.Actor;
import io.vlingo.actors.DeadLetter;
import io.vlingo.actors.LocalMessage;
import io.vlingo.actors.Mailbox;

public class Dispatcher__Proxy implements Dispatcher {

  private static final String dispatchForRepresentation1 = "dispatchFor(io.vlingo.http.Context)";
  private static final String stopRepresentation2 = "stop()";

  private final Actor actor;
  private final Mailbox mailbox;

  public Dispatcher__Proxy(final Actor actor, final Mailbox mailbox){
    this.actor = actor;
    this.mailbox = mailbox;
  }

  public void dispatchFor(io.vlingo.http.Context arg0) {
    if (!actor.isStopped()) {
      final Consumer<Dispatcher> consumer = (actor) -> actor.dispatchFor(arg0);
      mailbox.send(new LocalMessage<Dispatcher>(actor, Dispatcher.class, consumer, dispatchForRepresentation1));
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, dispatchForRepresentation1));
    }
  }
  public void stop() {
    if (!actor.isStopped()) {
      final Consumer<Dispatcher> consumer = (actor) -> actor.stop();
      mailbox.send(new LocalMessage<Dispatcher>(actor, Dispatcher.class, consumer, stopRepresentation2));
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, stopRepresentation2));
    }
  }
  public boolean isStopped() {
    return actor.isStopped();
  }
}
