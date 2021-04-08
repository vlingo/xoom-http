// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource.feed;

import io.vlingo.xoom.actors.Actor;

public class FeedConfiguration {
  private Class<? extends Actor> feedClass;
  private int elements;
  private String feedURI;
  private String name;
  private int poolSize;

  public static FeedConfiguration define() {
    return new FeedConfiguration("", "", null, 0, 0);
  }

  public static FeedConfiguration defineWith(final String name, final String feedURI, final Class<? extends Actor> feedClass, final int elements, final int poolSize) {
    return new FeedConfiguration(name, feedURI, feedClass, elements, poolSize);
  }

  public FeedConfiguration withName(final String name) {
    this.name = name;
    return this;
  }

  public FeedConfiguration withFeedURL(final String feedURI) {
    this.feedURI = feedURI;
    return this;
  }

  public FeedConfiguration with(final Class<? extends Actor> feedClass) {
    this.feedClass = feedClass;
    return this;
  }

  public FeedConfiguration withElements(final int elements) {
    this.elements = elements;
    return this;
  }

  public FeedConfiguration withPoolSize(final int poolSize) {
    this.poolSize = poolSize;
    return this;
  }

  public Class<? extends Actor> feedClass() {
    return feedClass;
  }

  public int elements() {
    return elements;
  }

  public String feedURI() {
    return feedURI;
  }

  public String name() {
    return name;
  }

  public int poolSize() {
    return poolSize;
  }

  public boolean isConfigured() {
    return !name.isEmpty() && !feedURI.isEmpty() && feedClass != null && elements > 0 && poolSize > 0;
  }

  private FeedConfiguration(final String name, final String feedURI, Class<? extends Actor> feedClass, final int elements, final int poolSize) {
    this.name = name;
    this.feedURI = feedURI;
    this.feedClass = feedClass;
    this.elements = elements;
    this.poolSize = poolSize;
  }
}
