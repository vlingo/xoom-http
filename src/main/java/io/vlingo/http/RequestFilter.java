// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import io.vlingo.common.Tuple2;

/**
 * A {@code Filter} for {@code Request} handling.
 */
public abstract class RequestFilter extends Filter {
  /**
   * Construct my state.
   */
  protected RequestFilter() { }

  /**
   * Answer the {@code Request} to be propagated forward to the next {@code RequestFilter}
   * or to the {@code ResourceHandler}, and a {@code Boolean} indicating whether or not the
   * chain should continue or be short circuited. If the {@code Boolean} is true, the chain
   * will continue; if false, it will be short circuited.
   * @param request the Request to filter
   * @return {@code Tuple2<Request,Boolean>}
   */
  public abstract Tuple2<Request,Boolean> filter(final Request request);
}
