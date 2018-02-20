// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import io.vlingo.common.fn.Tuple3;
import io.vlingo.wire.message.Converters;

public class HttpRequest {
  public final HttpBody body;
  public final List<HttpHeader> headers;
  public final HttpMethod method;
  public final URI uri;
  public final HttpVersion version;
  
  // TODO: Currently supports only HTTP/1.1
  
  public static HttpRequest from(final ByteBuffer requestContent) {
    final List<String> lines = parseLines(requestContent);
    final Tuple3<HttpMethod, URI, HttpVersion> requestLine = parseRequestLine(lines);
    final List<HttpHeader> headers = parseHeaders(lines);
    final HttpBody body = parseBody(lines, 1 + headers.size() + 1); // request line (1), headers (x), blank-line (1)
    
    return new HttpRequest(requestLine._1, requestLine._2, requestLine._3, headers, body);
  }

  private static String actualRequestLinePart(final String[] parts, final int part, final String name) {
    int partCount = 0;
    for (int idx = 0; idx < parts.length; ++idx) {
      if (parts[idx].length() > 0) {
        if (++partCount == part) {
          return parts[idx];
        }
      }
    }
    throw new IllegalArgumentException(HttpResponse.BadRequest + "\n\nRequest line part missing: " + name);
  }

  private static HttpBody parseBody(final List<String> lines, final int startingBodyLine) {
    return HttpBody.from(lines.subList(startingBodyLine, lines.size()));
  }

  private static List<HttpHeader> parseHeaders(final List<String> lines) {
    final int possibleHeaders = lines.size() - 2;
    final List<HttpHeader> headers = new ArrayList<>(possibleHeaders);
    for (int idx = 1; idx <= possibleHeaders; ++idx) {
      final String line = lines.get(idx);
      if (line.isEmpty()) {
        break;
      }
      headers.add(HttpRequestHeader.from(line));
    }
    return Collections.unmodifiableList(headers);
  }

  private static List<String> parseLines(final ByteBuffer requestContent) {
    final String text = Converters.bytesToText(requestContent.array(), 0, requestContent.limit());
    final List<String> lines = new ArrayList<>();
    
    try (Scanner scanner = new Scanner(text)) {
      while (scanner.hasNextLine()) {
        lines.add(scanner.nextLine());
      }
    }
    
    if (lines.size() < 3) {
      throw new IllegalArgumentException(HttpResponse.BadRequest + "\n\nToo few request lines (3+ expected)");
    }
    
    return lines;
  }

  private static Tuple3<HttpMethod, URI, HttpVersion> parseRequestLine(final List<String> lines) {
    final String[] parts = lines.get(0).split(" ");
    
    try {
      final HttpMethod method = HttpMethod.from(actualRequestLinePart(parts, 1, "Method"));
      final URI uri = new URI(actualRequestLinePart(parts, 2, "URI/path"));
      final HttpVersion version = HttpVersion.from(actualRequestLinePart(parts, 3, "HTTP/version"));
      return Tuple3.from(method, uri, version);
    } catch (Exception e) {
      throw new IllegalArgumentException(HttpResponse.BadRequest + "\n\nParsing exception: " + e.getMessage(), e);
    }
  }

  public HttpHeader headerOf(final String name) {
    for (final HttpHeader header : headers) {
      if (header.name.equals(name)) {
        return header;
      }
    }
    return null;
  }

  HttpRequest(final HttpMethod method, final URI uri, final HttpVersion version, final List<HttpHeader> headers, final HttpBody body) {
    this.method = method;
    this.uri = uri;
    this.version = version;
    this.headers = headers;
    this.body = body;
  }
}
