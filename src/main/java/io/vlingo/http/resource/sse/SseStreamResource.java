// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.sse;

import static io.vlingo.http.Response.Status.Ok;
import static io.vlingo.http.ResponseHeader.correlationId;
import static io.vlingo.http.ResponseHeader.headers;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Definition;
import io.vlingo.actors.Stoppable;
import io.vlingo.actors.World;
import io.vlingo.common.Cancellable;
import io.vlingo.common.Scheduled;
import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.RequestHeader;
import io.vlingo.http.Response;
import io.vlingo.http.resource.ResourceHandler;
import io.vlingo.wire.channel.RequestResponseContext;

public class SseStreamResource extends ResourceHandler {
  private static final Map<String,SsePublisher> publishers = new ConcurrentHashMap<>();
  private final World world;

  public SseStreamResource(final World world) {
    this.world = world;
  }

  public void subscribeToStream(final String streamName, final Class<? extends Actor> feedClass, final int feedPayload, final int feedInterval, final String feedDefaultId) {
    final RequestResponseContext<?> clientContext = context().clientContext();

    clientContext.whenClosing(unsubscribeRequest());

    final String correlationId = context().request().headerValueOr(RequestHeader.XCorrelationID, "");

    final SseSubscriber subscriber =
            new SseSubscriber(
                    streamName,
                    new SseClient(clientContext),
                    correlationId,
                    context().request().headerValueOr(RequestHeader.LastEventID, ""));

    publisherFor(streamName, feedClass, feedPayload, feedInterval, feedDefaultId).subscribe(subscriber);

    completes().with(Response.of(Ok, headers(correlationId(correlationId))));
  }

  public void unsubscribeFromStream(final String streamName, final String id) {
    final SsePublisher publisher = publishers.get(streamName);
    if (publisher != null) {
      publisher.unsubscribe(new SseSubscriber(streamName, new SseClient(context().clientContext())));
    }    

    completes().with(Response.of(Ok));
  }

  private SsePublisher publisherFor(final String streamName, final Class<? extends Actor> feedClass, final int feedPayload, final int feedInterval, final String feedDefaultId) {
    SsePublisher publisher = publishers.get(streamName);
    if (publisher == null) {
      publisher = world.actorFor(SsePublisher.class, Definition.has(SsePublisherActor.class, Definition.parameters(streamName, feedClass, feedPayload, feedInterval, feedDefaultId)));
      final SsePublisher presentPublisher = publishers.putIfAbsent(streamName, publisher);
      if (presentPublisher != null) {
        publisher.stop();
        publisher = presentPublisher;
      }
    }
    return publisher;
  }

  private Request unsubscribeRequest() {
    try {
      final String unsubscribePath = context().request().uri.getPath() + "/" + context().clientContext().id();
      return Request.has(Method.DELETE).and(new URI(unsubscribePath));
    } catch (Exception e) {
      return null;
    }
  }


  //=====================================
  // SsePublisherActor
  //=====================================

  public static class SsePublisherActor extends Actor implements SsePublisher, Scheduled<Object>, Stoppable {
    private final Cancellable cancellable;
    private final SseFeed feed;
    private final String streamName;
    private final Map<String,SseSubscriber> subscribers;

    @SuppressWarnings("unchecked")
    public SsePublisherActor(final String streamName, final Class<? extends Actor> feedClass, final int feedPayload, final int feedInterval, final String feedDefaultId) {
      this.streamName = streamName;
      this.feed = stage().actorFor(SseFeed.class, Definition.has(feedClass, Definition.parameters(streamName, feedPayload, feedDefaultId)));
      this.subscribers = new HashMap<>();

      this.cancellable = stage().scheduler().schedule(selfAs(Scheduled.class), null, 10, feedInterval);

      logger().log("SsePublisher started for: " + this.streamName);
    }


    //=====================================
    // SsePublisher
    //=====================================

    public void subscribe(final SseSubscriber subscriber) {
      subscribers.put(subscriber.id(), subscriber);
    }

    public void unsubscribe(final SseSubscriber subscriber) {
      subscriber.close();
      subscribers.remove(subscriber.id());
    }


    //=====================================
    // Scheduled
    //=====================================

    @Override
    public void intervalSignal(final Scheduled<Object> scheduled, final Object data) {
      feed.to(subscribers.values());
    }


    //=====================================
    // Stoppable
    //=====================================

    @Override
    public void stop() {
      cancellable.cancel();

      unsubscribeAll();

      super.stop();
    }

    private void unsubscribeAll() {
      final Collection<SseSubscriber> all = subscribers.values();
      for (final SseSubscriber subscriber : all.toArray(new SseSubscriber[all.size()])) {
        unsubscribe(subscriber);
      }
    }
  }
}
