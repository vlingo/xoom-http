// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import java.net.URI;
import java.nio.ByteBuffer;

import io.vlingo.http.Header.Headers;

public class Request {
  public final Body body;
  public final Headers<RequestHeader> headers;
  public final Method method;
  public final URI uri;
  public final Version version;

  // TODO: Currently supports only HTTP/1.1

  public static Request from(final ByteBuffer requestContent) {
    return RequestParser.parse(requestContent);
  }

  public Header headerOf(final String name) {
    for (final Header header : headers) {
      if (header.name.equals(name)) {
        return header;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return "" + method + " " + uri + " "  + version + "\n" + headers + "\n" + body;
  }

  Request(final Method method, final URI uri, final Version version, final Headers<RequestHeader> headers, final Body body) {
    this.method = method;
    this.uri = uri;
    this.version = version;
    this.headers = headers;
    this.body = body;
  }
}
