// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http;

import java.util.Collections;
import java.util.List;

import io.vlingo.xoom.common.Tuple2;

/**
 * A processor for all {@code Filter} types.
 */
public class Filters {
  private final List<RequestFilter> requestFilters;
  private final List<ResponseFilter> responseFilters;
  private boolean stopped;

  /**
   * Answer a new {@code Filters} for {@code requestFilters} and {@code responseFilters}.
   * @param requestFilters the {@code List<RequestFilter>} of request filters
   * @param responseFilters the {@code List<ResponseFilter>} of response filters
   * @return Filters
   */
  public static Filters are(final List<RequestFilter> requestFilters, final List<ResponseFilter> responseFilters) {
    return new Filters(requestFilters, responseFilters);
  }

  /**
   * Answer an empty {@code Filters} instance.
   * @return Filters
   */
  public static Filters none() {
    return new Filters(Collections.emptyList(), Collections.emptyList());
  }

  /**
   * Answer any empty {@code List<RequestFilter>}.
   * @return {@code List<RequestFilter>}
   */
  public static List<RequestFilter> noRequestFilters() {
    return Collections.emptyList();
  }

  /**
   * Answer any empty {@code List<ResponseFilter>}.
   * @return {@code List<ResponseFilter>}
   */
  public static List<ResponseFilter> noResponseFilters() {
    return Collections.emptyList();
  }

  /**
   * Answer the {@code Request} resulting from any filtering.
   * @param request the Request incoming from the client
   * @return Request
   */
  public Request process(final Request request) {
    if (stopped) return request;

    Request current = request;
    for (final RequestFilter filter : requestFilters) {
      final Tuple2<Request, Boolean> answer = filter.filter(current);
      if (!answer._2) {
        return answer._1;
      }
      current = answer._1;
    }
    return current;
  }

  /**
   * Answer the {@code Response} resulting from any filtering.
   * @param response the Response outgoing from a ResourceHandler
   * @return Response
   */
  public Response process(final Response response) {
    if (stopped) return response;

    Response current = response;
    for (final ResponseFilter filter : responseFilters) {
      final Tuple2<Response, Boolean> answer = filter.filter(current);
      if (!answer._2) {
        return answer._1;
      }
      current = answer._1;
    }
    return current;
  }

  /**
   * Stop all filters.
   */
  public void stop() {
    if (stopped) return;

    stopped = true;
    for (final Filter filter : requestFilters) {
      filter.stop();
    }
    for (final Filter filter : responseFilters) {
      filter.stop();
    }
  }

  /**
   * Constructs my state.
   * @param requestFilters the {@code List<RequestFilter>} of request filters
   * @param responseFilters the {@code List<ResponseFilter>} of response filters
   */
  private Filters(final List<RequestFilter> requestFilters, final List<ResponseFilter> responseFilters) {
    this.requestFilters = requestFilters;
    this.responseFilters = responseFilters;
    this.stopped = false;
  }
}
