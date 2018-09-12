// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.sse;

public class SseEvent {
  public static final int NoRetry = -1;

  public final String comment;
  public final String data;
  public final String event;
  public final String id;
  public final int retry;

  public SseEvent(final String id, final String event) {
    this(id, event, null, -1, null);
  }

  public SseEvent(final String id, final String event, final String data) {
    this(id, event, data, -1, null);
  }

  public SseEvent(final String id, final String event, final String data, final int retry, final String comment) {
    this.id = id;
    this.event = event;
    this.data = data;
    this.retry = retry;
    this.comment = comment;
  }

  public boolean endOfStream() {
    return id != null && id.isEmpty();
  }

  public boolean hasId() {
    return id != null && !id.isEmpty();
  }

  public String sendable() {
    return toString();
  }

  public String toString() {
    final StringBuilder builder = new StringBuilder();

    if (comment != null) {
      builder.append(": ").append(flatten(comment)).append('\n');
    }
    if (id != null) {
      if (id.length() > 0) {
        builder.append("id: ").append(flatten(id)).append('\n');
      } else {
        builder.append("id").append('\n'); // end of stream
      }
    }
    if (event != null) {
      builder.append("event: ").append(flatten(event)).append('\n');
    }
    if (data != null) {
      for (final String value : data.split("\n")) {
        builder.append("data: ").append(value).append('\n');
      }
    }
    if (retry >= 0) {
      builder.append("retry: ").append(retry).append('\n');
    }

    return builder.append('\n').toString();
  }

  private String flatten(final String text) {
    return text.replace("\n", "");
  }

  public static class Builder {
    public String comment;
    public String data;
    public String event;
    public String id;
    public int retry = NoRetry;

    public static Builder instance() {
      return new Builder();
    }

    public Builder() { }

    public Builder clear() {
      data = null;
      event = null;
      id = null;
      retry = NoRetry;
      return this;
    }

    public Builder comment(final String comment) {
      this.comment = comment;
      return this;
    }

    public Builder data(final String data) {
      this.data = data;
      return this;
    }

    public Builder endOfStream() {
      this.id = "";
      return this;
    }

    public Builder event(final String event) {
      this.event = event;
      return this;
    }

    public Builder id(final String id) {
      this.id = id;
      return this;
    }

    public Builder id(final int id) {
      this.id = String.valueOf(id);
      return this;
    }

    public Builder id(final long id) {
      this.id = String.valueOf(id);
      return this;
    }

    public Builder retry(final int retry) {
      this.retry = retry;
      return this;
    }

    public SseEvent toEvent() {
      return new SseEvent(id, event, data, retry, comment);
    }
  }
}
