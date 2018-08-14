/*
 * Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.resource;

import java.util.Properties;

public class Configuration {
  private int port;
  private Sizing sizing;
  private Timing timing;

  public static Configuration define() {
    return new Configuration();
  }

  public static Configuration defineWith(final Properties properties) {
    return new Configuration(properties);
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
    this.timing = new Timing(10, 10, 100);
  }

  private Configuration(final Properties properties) {
    this();

    this.port = Integer.parseInt(properties.getProperty("server.http.port", String.valueOf(this.port)));
    final int dispatcherPoolSize = Integer.parseInt(properties.getProperty("server.dispatcher.pool", String.valueOf(this.sizing.dispatcherPoolSize)));
    final int maxBufferPoolSize = Integer.parseInt(properties.getProperty("server.buffer.pool.size", String.valueOf(this.sizing.maxBufferPoolSize)));
    final int maxMessageSize = Integer.parseInt(properties.getProperty("server.message.buffer.size", String.valueOf(this.sizing.maxMessageSize)));
    final int probeInterval = Integer.parseInt(properties.getProperty("server.probe.interval", String.valueOf(this.timing.probeInterval)));
    final long probeTimeout = Long.parseLong(properties.getProperty("server.probe.timeout", String.valueOf(this.timing.probeTimeout)));
    final long requestMissingContentTimeout = Long.parseLong(properties.getProperty("server.request.missing.content.timeout", String.valueOf(this.timing.requestMissingContentTimeout)));

    this.sizing = new Sizing(dispatcherPoolSize, maxBufferPoolSize, maxMessageSize);
    this.timing = new Timing(probeInterval, probeTimeout, requestMissingContentTimeout);
  }

  public static class Sizing {
    public final int dispatcherPoolSize;
    public final int maxBufferPoolSize;
    public final int maxMessageSize;

    public Sizing(final int dispatcherPoolSize, final int maxBufferPoolSize, final int maxMessageSize) {
      this.dispatcherPoolSize = dispatcherPoolSize;
      this.maxBufferPoolSize = maxBufferPoolSize;
      this.maxMessageSize = maxMessageSize;
    }

    public static Sizing define() {
      return new Sizing(10, 100, 65535);
    }

    public Sizing withDispatcherPoolSize(final int dispatcherPoolSize) {
      return new Sizing(dispatcherPoolSize, this.maxBufferPoolSize, this.maxMessageSize);
    }

    public Sizing withMaxBufferPoolSize(final int maxBufferPoolSize) {
      return new Sizing(this.dispatcherPoolSize, maxBufferPoolSize, this.maxMessageSize);
    }

    public Sizing withMaxMessageSize(final int maxMessageSize) {
      return new Sizing(this.dispatcherPoolSize, this.maxBufferPoolSize, maxMessageSize);
    }
  }

  public static class Timing {
    public final int probeInterval;
    public final long probeTimeout;
    public final long requestMissingContentTimeout;

    public Timing(final int probeInterval, final long probeTimeout, final long requestMissingContentTimeout) {
      this.probeInterval = probeInterval;
      this.probeTimeout = probeTimeout;
      this.requestMissingContentTimeout = requestMissingContentTimeout;
    }

    public static Timing define() {
      return new Timing(10, 10, 100);
    }

    public Timing withProbeInterval(final int probeInterval) {
      return new Timing(probeInterval, this.probeTimeout, this.requestMissingContentTimeout);
    }

    public Timing withProbeTimeout(final long probeTimeout) {
      return new Timing(this.probeInterval, probeTimeout, this.requestMissingContentTimeout);
    }

    public Timing withRequestMissingContentTimeout(final long requestMissingContentTimeout) {
      return new Timing(this.probeInterval, this.probeTimeout, requestMissingContentTimeout);
    }
  }
}
