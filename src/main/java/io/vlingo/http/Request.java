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
import java.util.List;
import java.util.Scanner;

import io.vlingo.common.fn.Tuple3;
import io.vlingo.http.Header.Headers;
import io.vlingo.wire.message.Converters;

public class Request {
  public final Body body;
  public final Headers<RequestHeader> headers;
  public final Method method;
  public final URI uri;
  public final Version version;
  
  // TODO: Currently supports only HTTP/1.1
  
  public static Request from(final ByteBuffer requestContent) {
    final List<String> lines = parseLines(requestContent);
    final Tuple3<Method, URI, Version> requestLine = parseRequestLine(lines);
    final Headers<RequestHeader> headers = parseHeaders(lines);
    final Body body = parseBody(lines, 1 + headers.size() + 1); // request line (1), headers (x), blank-line (1)
    
    return new Request(requestLine._1, requestLine._2, requestLine._3, headers, body);
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
    throw new IllegalArgumentException(Response.BadRequest + "\n\nRequest line part missing: " + name);
  }

  private static Body parseBody(final List<String> lines, final int startingBodyLine) {
    return Body.from(lines.subList(startingBodyLine, lines.size()));
  }

  private static Headers<RequestHeader> parseHeaders(final List<String> lines) {
    final int possibleHeaders = lines.size() - 2;
    final Headers<RequestHeader> headers = new Headers<>(possibleHeaders);
    for (int idx = 1; idx <= possibleHeaders; ++idx) {
      final String line = lines.get(idx);
      if (line.isEmpty()) {
        break;
      }
      headers.add(RequestHeader.from(line));
    }
    return headers;
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
      throw new IllegalArgumentException(Response.BadRequest + "\n\nToo few request lines (3+ expected)");
    }
    
    return lines;
  }

  private static Tuple3<Method, URI, Version> parseRequestLine(final List<String> lines) {
    final String[] parts = lines.get(0).split(" ");
    
    try {
      final Method method = Method.from(actualRequestLinePart(parts, 1, "Method"));
      final URI uri = new URI(actualRequestLinePart(parts, 2, "URI/path"));
      final Version version = Version.from(actualRequestLinePart(parts, 3, "HTTP/version"));
      return Tuple3.from(method, uri, version);
    } catch (Exception e) {
      throw new IllegalArgumentException(Response.BadRequest + "\n\nParsing exception: " + e.getMessage(), e);
    }
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
    return "Request[\n" + method + " " + version + " " + uri + "\n" + headers + "\n\n" + body + "\n]";
  }

  Request(final Method method, final URI uri, final Version version, final Headers<RequestHeader> headers, final Body body) {
    this.method = method;
    this.uri = uri;
    this.version = version;
    this.headers = headers;
    this.body = body;
  }
}
