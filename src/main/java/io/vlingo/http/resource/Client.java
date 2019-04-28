// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.util.List;

import io.vlingo.actors.Actor;
import io.vlingo.actors.Definition;
import io.vlingo.actors.RouterSpecification;
import io.vlingo.actors.Stage;
import io.vlingo.common.Completes;
import io.vlingo.http.Request;
import io.vlingo.http.Response;
import io.vlingo.wire.node.Address;
import io.vlingo.wire.node.AddressType;
import io.vlingo.wire.node.Host;

/**
 * Asynchronous HTTP client.
 */
public class Client {
  public static enum ClientConsumerType { Correlating, LoadBalancing, RoundRobin };

  private final Configuration configuration;
  private final ClientConsumer consumer;

  /**
   * Answer a new {@code Client} from the {@code configuration}.
   * @param configuration the Configuration
   * @param type the ClientConsumerType
   * @param poolSize the int size of the pool of workers
   * @return Client
   * @throws Exception when the Client cannot be created
   */
  public static Client using(final Configuration configuration, final ClientConsumerType type, final int poolSize) throws Exception {
    return new Client(configuration, type, poolSize);
  }

  /**
   * Answer a new {@code Client} from the {@code configuration}.
   * @param configuration the Configuration
   * @return Client
   * @throws Exception when the Client cannot be created
   */
  public static Client using(final Configuration configuration) throws Exception {
    return new Client(configuration);
  }


  /**
   * Constructs my default state from the {@code configuration}.
   * @param configuration the Configuration
   * @param type the ClientConsumerType
   * @param poolSize the int size of the pool of workers
   * @throws Exception when the Client cannot be created
   */
  public Client(final Configuration configuration, final ClientConsumerType type, final int poolSize) throws Exception {
    this.configuration = configuration;

    final Class<? extends Actor> clientConsumerType;
    final List<Object> parameters;

    switch (type) {
    case Correlating:
      clientConsumerType = ClientCorrelatingRequesterConsumerActor.class;
      parameters = Definition.parameters(configuration);
      break;
    case RoundRobin: {
      clientConsumerType = RoundRobinClientRequestConsumerActor.class;
      final Definition definition = Definition.has(ClientConsumerWorkerActor.class, Definition.parameters(configuration));
      final RouterSpecification<ClientConsumer> spec = new RouterSpecification<>(poolSize, definition, ClientConsumer.class);
      parameters = Definition.parameters(configuration, spec);
      break;
      }
    case LoadBalancing: {
      clientConsumerType = LoadBalancingClientRequestConsumerActor.class;
      final Definition definition = Definition.has(ClientConsumerWorkerActor.class, Definition.parameters(configuration));
      final RouterSpecification<ClientConsumer> spec = new RouterSpecification<>(poolSize, definition, ClientConsumer.class);
      parameters = Definition.parameters(configuration, spec);
      break;
      }
    default:
      throw new IllegalArgumentException("ClientConsumerType is not mapped: " + type);
    }

    this.consumer = configuration.stage.actorFor(ClientConsumer.class,Definition.has(clientConsumerType, parameters));
  }

  /**
   * Constructs my default state from the {@code configuration}.
   * @param configuration the Configuration
   * @throws Exception when the Client cannot be created
   */
  public Client(final Configuration configuration) throws Exception {
    this(configuration, ClientConsumerType.Correlating, 0);
  }

  /**
   * Close me.
   */
  public void close() {
    consumer.stop();
  }

  /**
   * Answer a {@code Completes<Respose>} as the eventual outcomes of the {@code request}.
   * @param request the Request to the server
   * @return {@code Completes<Respose>}
   */
  public Completes<Response> requestWith(final Request request) {
    final Completes<Response> completes =
            configuration.keepAlive ?
                    Completes.repeatableUsing(configuration.stage.scheduler()) :
                    Completes.using(configuration.stage.scheduler());
    consumer.requestWith(request, completes);
    return completes;
  }

  /**
   * Configuration used to create a {@code Client}.
   */
  public static class Configuration {
    public final Address addressOfHost;
    public final ResponseConsumer consumerOfUnknownResponses;
    public final boolean keepAlive;
    public final long probeInterval;
    public final int readBufferSize;
    public final int readBufferPoolSize;
    public final int writeBufferSize;
    public final Stage stage;

    /**
     * Answer the {@code Configuration} with defaults except for the
     * {@code consumerOfUnknownResponses}.
     * @param stage the Stage to host the Client
     * @param consumerOfUnknownResponses the ResponseConsumer of responses that cannot be associated with a given consumer
     * @return Configuration
     */
    public static Configuration defaultedExceptFor(
            final Stage stage,
            final ResponseConsumer consumerOfUnknownResponses) {
      return defaultedExceptFor(
              stage,
              Address.from(Host.of("localhost"), 8080, AddressType.NONE),
              consumerOfUnknownResponses);
    }

    /**
     * Answer the {@code Configuration} with defaults except for the
     * {@code addressOfHost} and {@code consumerOfUnknownResponses}.
     * @param stage the Stage to host the Client
     * @param addressOfHost the Address of the host server
     * @param consumerOfUnknownResponses the ResponseConsumer of responses that cannot be associated with a given consumer
     * @return Configuration
     */
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

    /**
     * Answer the {@code Configuration} with defaults except for the
     * {@code addressOfHost}, {@code consumerOfUnknownResponses},
     * {@code writeBufferSize}, and {@code readBufferSize}.
     * @param stage the Stage to host the Client
     * @param addressOfHost the Address of the host server
     * @param consumerOfUnknownResponses the ResponseConsumer of responses that cannot be associated with a given consumer
     * @param writeBufferSize the int size of the write buffer
     * @param readBufferSize the int size of the read buffer
     * @return Configuration
     */
    public static Configuration defaultedExceptFor(
            final Stage stage,
            final Address addressOfHost,
            final ResponseConsumer consumerOfUnknownResponses,
            final int writeBufferSize,
            final int readBufferSize) {
      return has(
              stage,
              addressOfHost,
              consumerOfUnknownResponses,
              false,
              10,
              writeBufferSize,
              10,
              readBufferSize);
    }

    /**
     * Answer the {@code Configuration} for {@code keep-alive} mode with defaults
     * except for the {@code addressOfHost} and {@code consumerOfUnknownResponses}.
     * @param stage the Stage to host the Client
     * @param addressOfHost the Address of the host server
     * @param consumerOfUnknownResponses the ResponseConsumer of responses that cannot be associated with a given consumer
     * @return Configuration
     */
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

    /**
     * Answer the {@code Configuration} with the given options.
     * @param stage the Stage to host the Client
     * @param addressOfHost the Address of the host server
     * @param consumerOfUnknownResponses the ResponseConsumer of responses that cannot be associated with a given consumer
     * @param keepAlive the boolean indicating whether or not the connection is kept alive over multiple requests-responses
     * @param probeInterval the long number of milliseconds between each consumer channel probe
     * @param writeBufferSize the int size of the ByteBuffer used for writes/sends
     * @param readBufferPoolSize the int number of read buffers in the pool
     * @param readBufferSize the int size of the ByteBuffer used for reads/receives
     * @return Configuration
     */
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

    /**
     * Constructs my default state with the given options.
     * @param stage the Stage to host the Client
     * @param addressOfHost the Address of the host server
     * @param consumerOfUnknownResponses the ResponseConsumer of responses that cannot be associated with a given consumer
     * @param keepAlive the boolean indicating whether or not the connection is kept alive over multiple requests-responses
     * @param probeInterval the long number of milliseconds between each consumer channel probe
     * @param writeBufferSize the int size of the ByteBuffer used for writes/sends
     * @param readBufferPoolSize the int number of read buffers in the pool
     * @param readBufferSize the int size of the ByteBuffer used for reads/receives
     */
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
}
