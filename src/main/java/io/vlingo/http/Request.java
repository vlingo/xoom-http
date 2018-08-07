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
    return RequestParser.parserFor(requestContent).fullRequest();
  }

  // ===========================================
  // fluent API follows
  // ===========================================

  public static Request has(final Method method) {
    return new Request(method);
  }

  public Request and(final Body body) {
    return new Request(this.method, this.uri, this.version, this.headers, body);
  }

  public Request and(final RequestHeader header) {
    final Headers<RequestHeader> headers = Headers.empty();
    return new Request(this.method, this.uri, this.version, headers.and(this.headers).and(header), this.body);
  }

  public Request and(final Headers<RequestHeader> headers) {
    return new Request(this.method, this.uri, this.version, headers, this.body);
  }

  public Request and(final URI uri) {
    return new Request(this.method, uri, this.version, this.headers, this.body);
  }

  public Request and(final Version version) {
    return new Request(this.method, this.uri, version, this.headers, this.body);
  }

  // ===========================================
  // less fluent API follows
  // ===========================================

  public static Request method(final Method method) {
    return new Request(method);
  }

  public Request body(final String body) {
    return new Request(this.method, this.uri, this.version, this.headers, Body.from(body));
  }

  public Request header(final String name, final String value) {
    final Headers<RequestHeader> headers = Headers.empty();
    return new Request(this.method, this.uri, this.version, headers.and(this.headers).and(RequestHeader.of(name, value)), this.body);
  }

  public Request header(final String name, final int value) {
    return header(name, String.valueOf(value));
  }

  public Request uri(final String uri) {
    return new Request(this.method, URI.create(uri), this.version, this.headers, this.body);
  }

  public Request version(final String version) {
    return new Request(this.method, this.uri, Version.from(version), this.headers, this.body);
  }

  // ===========================================
  // instance
  // ===========================================

  public Header headerOf(final String name) {
    for (final Header header : headers) {
      if (header.name.equals(name)) {
        return header;
      }
    }
    return null;
  }

  public QueryParameters queryParameters() {
    return new QueryParameters(uri.getQuery());
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

  private Request(final Method method) {
    this(method, URI.create("/"), Version.Http1_1, Headers.empty(), Body.from(""));
  }
}
