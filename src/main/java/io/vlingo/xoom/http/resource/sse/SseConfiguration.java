// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource.sse;

import io.vlingo.xoom.actors.Actor;

public class SseConfiguration {
  private String defaultId;
  private Class<? extends Actor> feedClass;
  private int interval;
  private String name;
  private int payloadCount;
  private int poolSize;
  private String streamURI;

  public static SseConfiguration define() {
    return new SseConfiguration("", "", null, "-1", 0, 0, 0);
  }

  public static SseConfiguration defineWith(final String name, final String streamURI, final Class<? extends Actor> feedClass, final String defaultId, final int payloadCount, final int interval, final int poolSize) {
    return new SseConfiguration(name, streamURI, feedClass, defaultId, payloadCount, interval, poolSize);
  }

  public SseConfiguration withName(final String name) {
    this.name = name;
    return this;
  }

  public SseConfiguration withStreamURL(final String streamURI) {
    this.streamURI = streamURI;
    return this;
  }

  public SseConfiguration with(final Class<? extends Actor> feedClass) {
    this.feedClass = feedClass;
    return this;
  }

  public SseConfiguration withDefaultId(final String defaultId) {
    this.defaultId = defaultId;
    return this;
  }

  public SseConfiguration withPayloadCount(final int payloadCount) {
    this.payloadCount = payloadCount;
    return this;
  }

  public SseConfiguration withInterval(final int interval) {
    this.interval = interval;
    return this;
  }

  public SseConfiguration withPoolSize(final int poolSize) {
    this.poolSize = poolSize;
    return this;
  }

  public String defaultId() {
    return defaultId;
  }

  public Class<? extends Actor> feedClass() {
    return feedClass;
  }

  public int interval() {
    return interval;
  }

  public String name() {
    return name;
  }

  public int payloadCount() {
    return payloadCount;
  }

  public int poolSize() {
    return poolSize;
  }

  public String streamURI() {
    return streamURI;
  }

  public boolean isConfigured() {
    return !streamURI.isEmpty() && feedClass != null && payloadCount > 0 && interval > 0 && poolSize > 0;
  }

  private SseConfiguration(final String name, final String streamURI, Class<? extends Actor> feedClass, final String defaultId, final int payloadCount, final int interval, final int poolSize) {
    this.name = name;
    this.streamURI = streamURI;
    this.feedClass = feedClass;
    this.defaultId = defaultId;
    this.payloadCount = payloadCount;
    this.interval = interval;
    this.poolSize = poolSize;
  }
}
