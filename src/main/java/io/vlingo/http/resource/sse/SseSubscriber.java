// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.sse;

public class SseSubscriber {
  private final SseClient client;
  private String currentEventId;
  private final String streamName;

  public SseSubscriber(final String streamName, final SseClient client, final String lastEventId) {
    this.streamName = streamName;
    this.client = client;
    this.currentEventId = lastEventId;
  }

  public SseSubscriber(final String streamName, final SseClient client) {
    this(streamName, client, "");
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

  public String currentEventId() {
    return currentEventId;
  }

  public void currentEventId(final String currentEventId) {
    this.currentEventId = currentEventId;
  }

  public String id() {
    return client.id();
  }

  public String streamName() {
    return streamName;
  }
}
