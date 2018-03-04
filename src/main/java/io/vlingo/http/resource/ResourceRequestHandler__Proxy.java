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

public class ResourceRequestHandler__Proxy implements ResourceRequestHandler {

  private static final String handleForRepresentation1 = "handleFor(io.vlingo.http.Context, java.util.function.Consumer)";

  private final Actor actor;
  private final Mailbox mailbox;

  public ResourceRequestHandler__Proxy(final Actor actor, final Mailbox mailbox){
    this.actor = actor;
    this.mailbox = mailbox;
  }

  @SuppressWarnings("rawtypes")
  public void handleFor(io.vlingo.http.Context arg0, java.util.function.Consumer arg1) {
    if (!actor.isStopped()) {
      final Consumer<ResourceRequestHandler> consumer = (actor) -> actor.handleFor(arg0, arg1);
      mailbox.send(new LocalMessage<ResourceRequestHandler>(actor, ResourceRequestHandler.class, consumer, handleForRepresentation1));
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, handleForRepresentation1));
    }
  }
}
