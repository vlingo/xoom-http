// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource.sse;

import static io.vlingo.xoom.http.Response.Status.Ok;
import static io.vlingo.xoom.http.ResponseHeader.correlationId;
import static io.vlingo.xoom.http.ResponseHeader.headers;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.vlingo.xoom.actors.Actor;
import io.vlingo.xoom.actors.ActorInstantiator;
import io.vlingo.xoom.actors.ActorInstantiatorRegistry;
import io.vlingo.xoom.actors.Definition;
import io.vlingo.xoom.actors.Stoppable;
import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.common.Cancellable;
import io.vlingo.xoom.common.Scheduled;
import io.vlingo.xoom.http.Header.Headers;
import io.vlingo.xoom.http.Method;
import io.vlingo.xoom.http.Request;
import io.vlingo.xoom.http.RequestHeader;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.ResponseHeader;
import io.vlingo.xoom.http.resource.ResourceHandler;
import io.vlingo.xoom.http.resource.sse.SsePublisher.SsePublisherInstantiator;
import io.vlingo.xoom.wire.channel.RequestResponseContext;

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
    final Headers<ResponseHeader> headers = headers(correlationId(correlationId));

    final SseSubscriber subscriber =
            new SseSubscriber(
                    streamName,
                    new SseClient(clientContext, headers),
                    correlationId,
                    context().request().headerValueOr(RequestHeader.LastEventID, ""));

    publisherFor(streamName, feedClass, feedPayload, feedInterval, feedDefaultId).subscribe(subscriber);
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
      publisher = world.actorFor(SsePublisher.class, Definition.has(SsePublisherActor.class, new SsePublisherInstantiator(streamName, feedClass, feedPayload, feedInterval, feedDefaultId)));
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
      this.subscribers = new HashMap<>();

      final ActorInstantiator<?> instantiator = ActorInstantiatorRegistry.instantiatorFor(feedClass);
      if(instantiator==null)throw new IllegalArgumentException("No ActorInstantiator registred for feedClass="+feedClass.toString());
      instantiator.set("feedClass", feedClass);
      instantiator.set("streamName", streamName);
      instantiator.set("feedPayload", feedPayload);
      instantiator.set("feedDefaultId", feedDefaultId);

      this.feed = stage().actorFor(SseFeed.class, Definition.has(feedClass, instantiator));

      this.cancellable = stage().scheduler().schedule(selfAs(Scheduled.class), null, 10, feedInterval);

      logger().info("SsePublisher started for: " + this.streamName);
    }


    //=====================================
    // SsePublisher
    //=====================================

    @Override
    public void subscribe(final SseSubscriber subscriber) {
      subscribers.put(subscriber.id(), subscriber);
    }

    @Override
    public void unsubscribe(final SseSubscriber subscriber) {
      final SseSubscriber actual = subscribers.remove(subscriber.id());
      if (actual != null) {
        actual.close();
      }
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
