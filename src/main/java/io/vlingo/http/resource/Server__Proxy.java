// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.util.function.Consumer;

import io.vlingo.actors.Actor;
import io.vlingo.actors.BasicCompletes;
import io.vlingo.actors.Completes;
import io.vlingo.actors.DeadLetter;
import io.vlingo.actors.LocalMessage;
import io.vlingo.actors.Mailbox;

public class Server__Proxy implements Server {

  private static final String shutDownRepresentation1 = "shutDown()";
  private static final String startUpDownRepresentation2 = "startUp()";
  private static final String stopRepresentation3 = "stop()";

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
  public Completes<Boolean> shutDown() {
    if (!actor.isStopped()) {
      final Consumer<Server> consumer = (actor) -> actor.shutDown();
      final Completes<Boolean> completes = new BasicCompletes<>(actor.scheduler());
      mailbox.send(new LocalMessage<Server>(actor, Server.class, consumer, completes, shutDownRepresentation1));
      return completes;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, shutDownRepresentation1));
    }
    return null;
  }

  @Override
  public Completes<Boolean> startUp() {
    if (!actor.isStopped()) {
      final Consumer<Server> consumer = (actor) -> actor.startUp();
      final Completes<Boolean> completes = new BasicCompletes<>(actor.scheduler());
      mailbox.send(new LocalMessage<Server>(actor, Server.class, consumer, completes, startUpDownRepresentation2));
      return completes;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, startUpDownRepresentation2));
    }
    return null;
  }

  @Override
  public void stop() {
    if (!actor.isStopped()) {
      final Consumer<Server> consumer = (actor) -> actor.stop();
      mailbox.send(new LocalMessage<Server>(actor, Server.class, consumer, stopRepresentation3));
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, stopRepresentation3));
    }
  }
}
