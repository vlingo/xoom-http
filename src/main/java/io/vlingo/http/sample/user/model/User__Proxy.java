package io.vlingo.http.sample.user.model;

import java.util.function.Consumer;

import io.vlingo.actors.Actor;
import io.vlingo.actors.BasicCompletes;
import io.vlingo.actors.Completes;
import io.vlingo.actors.DeadLetter;
import io.vlingo.actors.LocalMessage;
import io.vlingo.actors.Mailbox;

public class User__Proxy implements User {

  private static final String withContactRepresentation1 = "withContact(io.vlingo.http.sample.user.model.Contact)";
  private static final String withNameRepresentation2 = "withName(io.vlingo.http.sample.user.model.Name)";

  private final Actor actor;
  private final Mailbox mailbox;

  public User__Proxy(final Actor actor, final Mailbox mailbox){
    this.actor = actor;
    this.mailbox = mailbox;
  }

  public Completes<State> withContact(io.vlingo.http.sample.user.model.Contact arg0) {
    if (!actor.isStopped()) {
      final Consumer<User> consumer = (actor) -> actor.withContact(arg0);
      final Completes<State> completes = new BasicCompletes<>(actor.scheduler());
      mailbox.send(new LocalMessage<User>(actor, User.class, consumer, completes, withContactRepresentation1));
      return completes;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, withContactRepresentation1));
    }
    return null;
  }
  public Completes<State> withName(io.vlingo.http.sample.user.model.Name arg0) {
    if (!actor.isStopped()) {
      System.out.println("PROXY ACTOR: " + actor.getClass());
      final Consumer<User> consumer = (actor) -> actor.withName(arg0);
      final Completes<State> completes = new BasicCompletes<>(actor.scheduler());
      mailbox.send(new LocalMessage<User>(actor, User.class, consumer, completes, withNameRepresentation2));
      return completes;
    } else {
      actor.deadLetters().failedDelivery(new DeadLetter(actor, withNameRepresentation2));
    }
    return null;
  }
}
