// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource.sse;

public class SseSubscriber {
  private final SseClient client;
  private final String correlationId;
  private String currentEventId;
  private final String streamName;

  public SseSubscriber(final String streamName, final SseClient client, final String correlationId, final String lastEventId) {
    this.streamName = streamName;
    this.client = client;
    this.correlationId = correlationId;
    this.currentEventId = lastEventId;
  }

  public SseSubscriber(final String streamName, final SseClient client) {
    this(streamName, client, "", "");
  }

  public SseClient client() {
    return client;
  }

  public void close() {
    client.close();
  }

  public boolean isCompatibleWith(final String streamName) {
    return this.streamName.equals(streamName);
  }

  public String correlationId() {
    return correlationId;
  }

  public boolean hasCorrelationId() {
    return correlationId != null && !correlationId.isEmpty();
  }

  public String currentEventId() {
    return currentEventId;
  }

  public void currentEventId(final String currentEventId) {
    this.currentEventId = currentEventId;
  }

  public boolean hasCurrentEventId() {
    return currentEventId != null && !currentEventId.isEmpty();
  }

  public String id() {
    return client.id();
  }

  public String streamName() {
    return streamName;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("SseSubscriber [");
    sb.append("stream='").append(streamName()).append('\'');
    if (hasCorrelationId()) {
      sb.append(", correlationId='").append(correlationId()).append('\'');
    }
    if (hasCurrentEventId()) {
      sb.append(", currentEventId='").append(currentEventId()).append('\'');
    }
    if (client.id()!=null) {
      sb.append(", client=").append(client.id());
    }
    sb.append(']');
    return sb.toString();
  }
}
