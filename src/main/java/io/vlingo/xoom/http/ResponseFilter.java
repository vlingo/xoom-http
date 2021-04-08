// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http;

import io.vlingo.xoom.common.Tuple2;

/**
 * A {@code Filter} for {@code Response} handling.
 */
public abstract class ResponseFilter extends Filter {
  /**
   * Construct my state.
   */
  protected ResponseFilter() { }

  /**
   * Answer the {@code Response} to be propagated forward to the next {@code ResponseFilter}
   * or as the final {@code Response}, and a {@code Boolean} indicating whether or not the
   * chain should continue or be short circuited. If the {@code Boolean} is true, the chain
   * will continue; if false, it will be short circuited.
   * @param request the Request to filter
   * @return {@code Tuple2<Request,Boolean>}
   */
  public abstract Tuple2<Response,Boolean> filter(final Response request);
}
