// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import java.net.URI;
import java.nio.ByteBuffer;

import io.vlingo.common.fn.Tuple3;
import io.vlingo.http.Header.Headers;
import io.vlingo.wire.message.Converters;

public class RequestParser {

  public static Request parse(final ByteBuffer requestContent) {
    final RequestParserState state = new RequestParserState(Converters.bytesToText(requestContent.array(), 0, requestContent.limit()));
    final Tuple3<Method, URI, Version> requestLine = parseRequestLine(state);
    final Headers<RequestHeader> headers = parseHeaders(state);
    final Body body = parseBody(state);
    return new Request(requestLine._1, requestLine._2, requestLine._3, headers, body);
  }

  private static String nextLine(final RequestParserState state, final String errorResult, final String errorMessage) {
    final int lineBreak = state.requestText.indexOf("\n", state.position);
    if (lineBreak <= 0) throw new IllegalArgumentException(errorResult + " " + errorMessage);
    final int endOfLine = state.requestText.charAt(lineBreak - 1) == '\r' ? lineBreak - 1 : lineBreak;
    final String line  = state.requestText.substring(state.position, endOfLine).trim();
    state.position = lineBreak + 1;
    return line;
  }

  private static Body parseBody(final RequestParserState state) {
    if (state.contentLength > 0) {
      return Body.from(state.requestText.substring(state.position, state.position + state.contentLength));
    }
    return Body.from("");
  }

  private static Headers<RequestHeader> parseHeaders(final RequestParserState state) {
    final Headers<RequestHeader> headers = new Headers<>(2);
    while (true) {
      final String maybeHeaderLine = nextLine(state, Response.BadRequest, "\n\nHeader is required.");
      if (maybeHeaderLine.isEmpty()) {
        break;
      }
      final RequestHeader header = RequestHeader.from(maybeHeaderLine);
      headers.add(header);
      if (state.contentLength == 0) {
        final int maybeContentLength = header.ifContentLength();
        if (maybeContentLength > 0) {
          state.contentLength = maybeContentLength;
        }
      }
    }
    if (headers.isEmpty()) {
      throw new IllegalArgumentException(Response.BadRequest + "\n\nHeader is required.");
    }
    return headers;
  }

  private static Tuple3<Method, URI, Version> parseRequestLine(final RequestParserState state) {
    final String[] parts = nextLine(state, Response.BadRequest, "\n\nRequest line is required.").split(" ");

    try {
      final Method method = Method.from(specificRequestLinePart(parts, 1, "Method"));
      final URI uri = new URI(specificRequestLinePart(parts, 2, "URI/path"));
      final Version version = Version.from(specificRequestLinePart(parts, 3, "HTTP/version"));
      return Tuple3.from(method, uri, version);
    } catch (Exception e) {
      throw new IllegalArgumentException(Response.BadRequest + "\n\nParsing exception: " + e.getMessage(), e);
    }
  }

  private static String specificRequestLinePart(final String[] parts, final int part, final String name) {
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

  static class RequestParserState {
    int contentLength;
    int position;
    final String requestText;
    Method method;
    URI uri;
    Version version;
    
    RequestParserState(final String requestText) {
      this.requestText = requestText;
    }
  }
}
