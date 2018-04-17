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

public class Server__Proxy implements Server {

  private static final String stopRepresentation1 = "stop()";

  private final Actor actor;
  private final Mailbox mailbox;

  public Server__Proxy(final Actor actor, final Mailbox mailbox){
    this.actor = actor;
    this.mailbox = mailbox;
  }

  @Override
  public boolean isStopped() {
    return actor.isStopped();
  }

  @Override
  public void stop() {
    if (!actor.isStopped()) {
      final Consumer<Server> consumer = (actor) -> actor.stop();
      mailbox.send(new LocalMessage<Server>(actor, Server.class, consumer, stopRepresentation1));
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, stopRepresentation1));
    }
  }
}
