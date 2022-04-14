// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import io.vlingo.xoom.actors.*;
import io.vlingo.xoom.common.SerializableConsumer;

public class RequestSender__Proxy implements io.vlingo.xoom.http.resource.RequestSender {

  private static final String representationConclude0 = "conclude()";
  private static final String sendRequestRepresentation1 = "sendRequest(io.vlingo.xoom.http.Request)";
  private static final String stopRepresentation2 = "stop()";

  private final Actor actor;
  private final Mailbox mailbox;

  public RequestSender__Proxy(final Actor actor, final Mailbox mailbox){
    this.actor = actor;
    this.mailbox = mailbox;
  }

  @Override
  public void sendRequest(io.vlingo.xoom.http.Request arg0) {
    if (!actor.isStopped()) {
      final SerializableConsumer<RequestSender> consumer = (actor) -> actor.sendRequest(arg0);
      if (mailbox.isPreallocated()) { mailbox.send(actor, RequestSender.class, consumer, null, sendRequestRepresentation1); }
      else { mailbox.send(new LocalMessage<RequestSender>(actor, RequestSender.class, consumer, sendRequestRepresentation1)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, sendRequestRepresentation1));
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
      final SerializableConsumer<RequestSender> consumer = (actor) -> actor.stop();
      if (mailbox.isPreallocated()) { mailbox.send(actor, RequestSender.class, consumer, null, stopRepresentation2); }
      else { mailbox.send(new LocalMessage<RequestSender>(actor, RequestSender.class, consumer, stopRepresentation2)); }
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, stopRepresentation2));
    }
  }
  @Override
  public boolean isStopped() {
    return actor.isStopped();
  }
}
