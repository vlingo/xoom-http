/*
 * Copyright © 2012-2021 VLINGO LABS. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.xoom.http.resource;

import java.util.Properties;

public class Configuration {
  public static Configuration instance;

  private int port;
  private Sizing sizing;
  private Timing timing;

  public static Configuration define() {
    instance = new Configuration();
    return instance;
  }

  public static Configuration defineWith(final Properties properties) {
    instance = new Configuration(properties);
    return instance;
  }

  public Configuration withPort(final int port) {
    this.port = port;
    return this;
  }

  public Configuration with(final Sizing sizing) {
    this.sizing = sizing;
    return this;
  }

  public Configuration with(final Timing timing) {
    this.timing = timing;
    return this;
  }

  public int port() {
    return this.port;
  }

  public Sizing sizing() {
    return this.sizing;
  }

  public Timing timing() {
    return this.timing;
  }

  private Configuration() {
    this.port = 8080;
    this.sizing = Sizing.define();
    this.timing = new Timing(4, 2, 100);
  }

  private Configuration(final Properties properties) {
    this();

    this.port = Integer.parseInt(properties.getProperty("server.http.port", String.valueOf(this.port)));
    final int processorPoolSize = Integer.parseInt(properties.getProperty("server.processor.pool.size", String.valueOf(this.sizing.processorPoolSize)));
    final int dispatcherPoolSize = Integer.parseInt(properties.getProperty("server.dispatcher.pool", String.valueOf(this.sizing.dispatcherPoolSize)));
    final int maxBufferPoolSize = Integer.parseInt(properties.getProperty("server.buffer.pool.size", String.valueOf(this.sizing.maxBufferPoolSize)));
    final int maxMessageSize = Integer.parseInt(properties.getProperty("server.message.buffer.size", String.valueOf(this.sizing.maxMessageSize)));
    final long probeInterval = Long.parseLong(properties.getProperty("server.probe.interval", String.valueOf(this.timing.probeInterval)));
    final long probeTimeout = Long.parseLong(properties.getProperty("server.probe.timeout", String.valueOf(this.timing.probeInterval)));
    final long requestMissingContentTimeout = Long.parseLong(properties.getProperty("server.request.missing.content.timeout", String.valueOf(this.timing.requestMissingContentTimeout)));

    this.sizing = new Sizing(processorPoolSize, dispatcherPoolSize, maxBufferPoolSize, maxMessageSize);
    this.timing = new Timing(probeInterval, probeTimeout, requestMissingContentTimeout);
  }

  public static class Sizing {
    public final int processorPoolSize;
    public final int dispatcherPoolSize;
    public final int maxBufferPoolSize;
    public final int maxMessageSize;

    public Sizing(final int processorPoolSize, final int dispatcherPoolSize, final int maxBufferPoolSize, final int maxMessageSize) {
      this.processorPoolSize = processorPoolSize;
      this.dispatcherPoolSize = dispatcherPoolSize;
      this.maxBufferPoolSize = maxBufferPoolSize;
      this.maxMessageSize = maxMessageSize;
    }

    public static Sizing define() {
      return new Sizing(10, 10, 100, 65535);
    }

    public static Sizing defineWith(final int processorPoolSize, final int dispatcherPoolSize, final int maxBufferPoolSize, final int maxMessageSize) {
      return new Sizing(processorPoolSize, dispatcherPoolSize, maxBufferPoolSize, maxMessageSize);
    }

    public Sizing withProcessorPoolSize(final int processorPoolSize) {
      return new Sizing(processorPoolSize, this.dispatcherPoolSize, this.maxBufferPoolSize, this.maxMessageSize);
    }

    public Sizing withDispatcherPoolSize(final int dispatcherPoolSize) {
      return new Sizing(this.processorPoolSize, dispatcherPoolSize, this.maxBufferPoolSize, this.maxMessageSize);
    }

    public Sizing withMaxBufferPoolSize(final int maxBufferPoolSize) {
      return new Sizing(this.processorPoolSize, this.dispatcherPoolSize, maxBufferPoolSize, this.maxMessageSize);
    }

    public Sizing withMaxMessageSize(final int maxMessageSize) {
      return new Sizing(this.processorPoolSize, this.dispatcherPoolSize, this.maxBufferPoolSize, maxMessageSize);
    }
  }

  public static class Timing {
    public final long probeInterval;
    public final long probeTimeout;
    public final long requestMissingContentTimeout;

    public Timing(final long probeInterval, final long probeTimeout, final long requestMissingContentTimeout) {
      this.probeInterval = probeInterval;
      this.probeTimeout = probeTimeout;
      this.requestMissingContentTimeout = requestMissingContentTimeout;
    }

    public static Timing define() {
      // Faster, but may not work on some hardware/OS:
      //   new Timing(3, 2, 100);

      return new Timing(7, 3, 100);
    }

    public static Timing defineWith(final long probeInterval, final long probeTimeout, final long requestMissingContentTimeout) {
      return new Timing(probeInterval, probeTimeout, requestMissingContentTimeout);
    }

    public Timing withProbeInterval(final int probeInterval) {
      return new Timing(probeInterval, this.probeTimeout, this.requestMissingContentTimeout);
    }

    public Timing withProbeTimeout(final int probeTimeout) {
      return new Timing(this.probeInterval, probeTimeout, this.requestMissingContentTimeout);
    }

    public Timing withRequestMissingContentTimeout(final long requestMissingContentTimeout) {
      return new Timing(this.probeInterval, this.probeTimeout, requestMissingContentTimeout);
    }
  }
}
