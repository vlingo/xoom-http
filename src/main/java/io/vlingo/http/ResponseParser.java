// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import java.nio.ByteBuffer;

import io.vlingo.common.fn.Tuple2;
import io.vlingo.http.Header.Headers;
import io.vlingo.wire.message.Converters;

public class ResponseParser {

  public static Response parse(final ByteBuffer requestContent) {
    final ResponseParserState state = new ResponseParserState(Converters.bytesToText(requestContent.array(), 0, requestContent.limit()));
    final Tuple2<Version, String> responseLine = parseStatusLine(state);
    final Headers<ResponseHeader> headers = parseHeaders(state);
    final Body body = parseBody(state);
    return Response.of(responseLine._1, responseLine._2, headers, body);
  }

  private static String nextLine(final ResponseParserState state, final String errorResult, final String errorMessage) {
    final int lineBreak = state.responseText.indexOf("\n", state.position);
    if (lineBreak <= 0) {
      if (errorResult != null) {
        throw new IllegalArgumentException(errorResult + " " + errorMessage);
      }
      return "";
    }
    final int endOfLine = state.responseText.charAt(lineBreak - 1) == '\r' ? lineBreak - 1 : lineBreak;
    final String line  = state.responseText.substring(state.position, endOfLine).trim();
    state.position = lineBreak + 1;
    return line;
  }

  private static Body parseBody(final ResponseParserState state) {
    if (state.contentLength > 0) {
      return Body.from(state.responseText.substring(state.position, state.position + state.contentLength));
    }
    return Body.from("");
  }

  private static Headers<ResponseHeader> parseHeaders(final ResponseParserState state) {
    final Headers<ResponseHeader> headers = new Headers<>(2);
    while (true) {
      final String maybeHeaderLine = nextLine(state, null, null);
      if (maybeHeaderLine.isEmpty()) {
        break;
      }
      final ResponseHeader header = ResponseHeader.from(maybeHeaderLine);
      headers.add(header);
      if (state.contentLength == 0) {
        final int maybeContentLength = header.ifContentLength();
        if (maybeContentLength > 0) {
          state.contentLength = maybeContentLength;
        }
      }
    }
    return headers;
  }

  private static Tuple2<Version, String> parseStatusLine(final ResponseParserState state) {
    final String[] parts = nextLine(state, Response.BadRequest, "\n\nResponse line is required.").split(" ");

    try {
      return Tuple2.from(Version.from(parts[0]), parts[1]);
    } catch (Exception e) {
      throw new IllegalArgumentException(Response.BadRequest + "\n\nParsing exception: " + e.getMessage(), e);
    }
  }

  static class ResponseParserState {
    int contentLength;
    int position;
    final String responseText;
    Version version;
    
    ResponseParserState(final String responseText) {
      this.responseText = responseText;
    }
  }
}
