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
import io.vlingo.http.resource.Client.ClientConsumer;
import io.vlingo.http.Response;

public class ClientClientConsumer__Proxy implements ClientConsumer {

  private static final String requestWithRepresentation1 = "requestWith(io.vlingo.http.Request)";
  private static final String consumeRepresentation2 = "consume(io.vlingo.wire.message.ConsumerByteBuffer)";
  private static final String intervalSignalRepresentation3 = "intervalSignal(io.vlingo.actors.Scheduled, java.lang.Object)";
  private static final String stopRepresentation4 = "stop()";
  private static final String isStoppedRepresentation5 = "isStopped()";

  private final Actor actor;
  private final Mailbox mailbox;

  public ClientClientConsumer__Proxy(final Actor actor, final Mailbox mailbox){
    this.actor = actor;
    this.mailbox = mailbox;
  }

  public Completes<Response> requestWith(io.vlingo.http.Request arg0) {
    if (!actor.isStopped()) {
      final Consumer<ClientConsumer> consumer = (actor) -> actor.requestWith(arg0);
      final Completes<Response> completes = new BasicCompletes<>(actor.scheduler());
      mailbox.send(new LocalMessage<ClientConsumer>(actor, ClientConsumer.class, consumer, completes, requestWithRepresentation1));
      return completes;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, requestWithRepresentation1));
    }
    return null;
  }
  public void consume(io.vlingo.wire.message.ConsumerByteBuffer arg0) {
    if (!actor.isStopped()) {
      final Consumer<ClientConsumer> consumer = (actor) -> actor.consume(arg0);
      mailbox.send(new LocalMessage<ClientConsumer>(actor, ClientConsumer.class, consumer, consumeRepresentation2));
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, consumeRepresentation2));
    }
  }
  public void intervalSignal(io.vlingo.actors.Scheduled arg0, java.lang.Object arg1) {
    if (!actor.isStopped()) {
      final Consumer<ClientConsumer> consumer = (actor) -> actor.intervalSignal(arg0, arg1);
      mailbox.send(new LocalMessage<ClientConsumer>(actor, ClientConsumer.class, consumer, intervalSignalRepresentation3));
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, intervalSignalRepresentation3));
    }
  }
  public void stop() {
    if (!actor.isStopped()) {
      final Consumer<ClientConsumer> consumer = (actor) -> actor.stop();
      mailbox.send(new LocalMessage<ClientConsumer>(actor, ClientConsumer.class, consumer, stopRepresentation4));
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, stopRepresentation4));
    }
  }
  public boolean isStopped() {
    if (!actor.isStopped()) {
      final Consumer<ClientConsumer> consumer = (actor) -> actor.isStopped();
      mailbox.send(new LocalMessage<ClientConsumer>(actor, ClientConsumer.class, consumer, isStoppedRepresentation5));
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, isStoppedRepresentation5));
    }
    return false;
  }
}
