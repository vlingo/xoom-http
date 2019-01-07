// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.vlingo.actors.Actor;
import io.vlingo.actors.CompletesEventually;
import io.vlingo.actors.Definition;
import io.vlingo.actors.Stage;
import io.vlingo.actors.Stoppable;
import io.vlingo.common.Cancellable;
import io.vlingo.common.Completes;
import io.vlingo.common.Scheduled;
import io.vlingo.http.Request;
import io.vlingo.http.RequestHeader;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseHeader;
import io.vlingo.http.ResponseParser;
import io.vlingo.wire.channel.ResponseChannelConsumer;
import io.vlingo.wire.fdx.bidirectional.ClientRequestResponseChannel;
import io.vlingo.wire.message.ByteBufferAllocator;
import io.vlingo.wire.message.ConsumerByteBuffer;
import io.vlingo.wire.message.Converters;
import io.vlingo.wire.node.Address;
import io.vlingo.wire.node.AddressType;
import io.vlingo.wire.node.Host;

public class Client {
  private final Configuration configuration;
  private final ClientConsumer consumer;

  public static Client using(final Configuration configuration) throws Exception {
    return new Client(configuration);
  }

  public Client(final Configuration configuration) throws Exception {
    this.configuration = configuration;
    this.consumer = configuration.stage.actorFor(
            ClientConsumer.class,
            Definition.has(ClientRequesterConsumerActor.class, Definition.parameters(configuration)));
  }

  public void close() {
    consumer.stop();
  }

  public Completes<Response> requestWith(final Request request) {
    final Completes<Response> completes =
            configuration.keepAlive ?
                    Completes.repeatableUsing(configuration.stage.scheduler()) :
                    Completes.using(configuration.stage.scheduler());
    consumer.requestWith(request, completes);
    return completes;
  }

  public static class Configuration {
    public final Address addressOfHost;
    public final ResponseConsumer consumerOfUnknownResponses;
    public final boolean keepAlive;
    public final long probeInterval;
    public final int readBufferSize;
    public final int readBufferPoolSize;
    public final int writeBufferSize;
    public final Stage stage;

    public static Configuration defaultedExceptFor(
            final Stage stage,
            final ResponseConsumer consumerOfUnknownResponses) {
      return defaultedExceptFor(
              stage,
              Address.from(Host.of("localhost"), 8080, AddressType.NONE),
              consumerOfUnknownResponses);
    }

    public static Configuration defaultedExceptFor(
            final Stage stage,
            final Address addressOfHost,
            final ResponseConsumer consumerOfUnknownResponses) {
      return has(
              stage,
              addressOfHost,
              consumerOfUnknownResponses,
              false,
              10,
              10240,
              10,
              10240);
    }

    public static Configuration defaultedKeepAliveExceptFor(
            final Stage stage,
            final Address addressOfHost,
            final ResponseConsumer consumerOfUnknownResponses) {
      return has(
              stage,
              addressOfHost,
              consumerOfUnknownResponses,
              true,
              10,
              10240,
              10,
              10240);
    }

    public static Configuration has(
            final Stage stage,
            final Address addressOfHost,
            final ResponseConsumer consumerOfUnknownResponses,
            final boolean keepAlive,
            final long probeInterval,
            final int writeBufferSize,
            final int readBufferPoolSize,
            final int readBufferSize) {
      return new Configuration(
              stage,
              addressOfHost,
              consumerOfUnknownResponses,
              keepAlive,
              probeInterval,
              writeBufferSize,
              readBufferPoolSize,
              readBufferSize);
    }

    public Configuration(
            final Stage stage,
            final Address addressOfHost,
            final ResponseConsumer consumerOfUnknownResponses,
            final boolean keepAlive,
            final long probeInterval,
            final int writeBufferSize,
            final int readBufferPoolSize,
            final int readBufferSize) {

      this.stage = stage;
      this.addressOfHost = addressOfHost;
      this.consumerOfUnknownResponses = consumerOfUnknownResponses;
      this.keepAlive = keepAlive;
      this.probeInterval = probeInterval;
      this.writeBufferSize = writeBufferSize;
      this.readBufferPoolSize = readBufferPoolSize;
      this.readBufferSize = readBufferSize;
    }
  }

  public static interface ClientConsumer extends ResponseChannelConsumer, Scheduled, Stoppable {
    Completes<Response> requestWith(final Request request, final Completes<Response> completes);
  }

  public static class ClientRequesterConsumerActor extends Actor implements ClientConsumer {
    private final ByteBuffer buffer;
    private final Map<String, CompletesEventually> completables;
    private final ClientRequestResponseChannel channel;
    private final Configuration configuration;
    private ResponseParser parser;
    private final Cancellable probe;

    public ClientRequesterConsumerActor(final Configuration configuration) throws Exception {
      this.configuration = configuration;
      this.buffer = ByteBufferAllocator.allocate(configuration.writeBufferSize);
      this.completables = new HashMap<>();
      this.channel = clientChannel(configuration);
      this.probe = stage().scheduler().schedule(selfAs(Scheduled.class), null, 1, configuration.probeInterval);
    }

    @Override
    public void consume(final ConsumerByteBuffer buffer) {
      if (parser == null) {
        parser = ResponseParser.parserFor(buffer.asByteBuffer());
      } else {
        parser.parseNext(buffer.asByteBuffer());
      }
      buffer.release();

      while (parser.hasFullResponse()) {
        final Response response = parser.fullResponse();
        final ResponseHeader correlationId = response.headers.headerOf(ResponseHeader.XCorrelationID);
        if (correlationId == null) {
          logger().log("Client Consumer: Cannot complete response because no correlation id.");
          configuration.consumerOfUnknownResponses.consume(response);
        } else {
          final CompletesEventually completes = configuration.keepAlive ?
                  completables.get(correlationId.value) :
                  completables.remove(correlationId.value);
          if (completes == null) {
            configuration.stage.world().defaultLogger().log(
                    "Client Consumer: Cannot complete response because mismatched correlation id: " +
                     correlationId.value);
            configuration.consumerOfUnknownResponses.consume(response);
          } else {
            completes.with(response);
          }
        }
      }
    }

    @Override
    public void intervalSignal(final Scheduled scheduled, final Object data) {
      channel.probeChannel();
    }

    @Override
    public Completes<Response> requestWith(final Request request, final Completes<Response> completes) {
      RequestHeader correlationId = request.headers.headerOf(RequestHeader.XCorrelationID);
      
      final Request readyRequest;
      
      if (correlationId == null) {
        correlationId = RequestHeader.of(RequestHeader.XCorrelationID, UUID.randomUUID().toString());
        readyRequest = request.and(correlationId);
      } else {
        readyRequest = request;
      }

      completables.put(correlationId.value, stage().world().completesFor(completes));

      buffer.clear();
      buffer.put(Converters.textToBytes(readyRequest.toString()));
      buffer.flip();
      channel.requestWith(buffer);

      return completes;
    }

    @Override
    public void stop() {
      channel.close();
      probe.cancel();
    }

    private ClientRequestResponseChannel clientChannel(final Configuration configuration) throws Exception {
      return new ClientRequestResponseChannel(
              configuration.addressOfHost,
              selfAs(ResponseChannelConsumer.class),
              configuration.readBufferPoolSize,
              configuration.readBufferSize,
              logger());
    }
  }
}
