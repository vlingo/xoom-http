// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource.feed;

import io.vlingo.xoom.http.Context;

/**
 * Defines a request for a feed product.
 */
public class FeedProductRequest {
  public final Context context;
  public final String feedName;
  public final int feedProductElements;
  public final String feedProductId;

  /**
   * Constructs my state
   * @param context the Context of the original HTTP request
   * @param feedName the String name of the feed from which the product is made
   * @param feedProductId the String identity of the product to feed
   * @param feedProductElements the int maximum number of elements in the product
   */
  public FeedProductRequest(final Context context, final String feedName, final String feedProductId, final int feedProductElements) {
    this.context = context;
    this.feedName = feedName;
    this.feedProductId = feedProductId;
    this.feedProductElements = feedProductElements;
  }
}
