// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.vlingo.xoom.common.Tuple2;

public class CORSResponseFilter extends ResponseFilter {
  private final Map<String, List<ResponseHeader>> originHeaders;

  public CORSResponseFilter() {
    this.originHeaders = new HashMap<>();
  }

  /**
   * Register the {@code responseHeaders} with the {@code originalURI} such that
   * when a {@code Request} contains a RequestHeader of type {@code ORIGIN}, the
   * {@code Response} will contain the {@code responseHeaders}.
   * @param originURI the String URI of a valid CORS origin
   * @param responseHeaders the {@code List<ResponseHeader>} to set in the Responses for {@code ORIGIN: URI}
   */
  public void originHeadersFor(final String originURI, final List<ResponseHeader> responseHeaders) {
    originHeaders.put(originURI, responseHeaders);
  }

  /**
   * WARNING: Use is not an error but will not alter the {@code Response}.
   *
   * @see io.vlingo.xoom.http.ResponseFilter#filter(io.vlingo.xoom.http.Response)
   */
  @Override
  public Tuple2<Response, Boolean> filter(final Response response) {
    return Tuple2.from(response, true); // not an error but won't alter the response
  }

  /**
   * @see io.vlingo.xoom.http.ResponseFilter#filter(io.vlingo.xoom.http.Request, io.vlingo.xoom.http.Response)
   */
  @Override
  public Tuple2<Response, Boolean> filter(final Request request, final Response response) {
    final String origin = request.headerValueOr(RequestHeader.Origin, null);

    if (origin != null) {
      for (final String uri : originHeaders.keySet()) {
        if (uri.equals(origin)) {
          response.includeAll(originHeaders.get(origin));
          break;
        }
      }
    }

    return Tuple2.from(response, true);
  }

  @Override
  public void stop() {
  }
}
