// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Queue;
import java.util.StringTokenizer;

import io.vlingo.xoom.http.Header.Headers;
import io.vlingo.xoom.wire.message.Converters;

public class RequestParser {
  private final VirtualStateParser virtualStateParser;

  public String currentRequestText() {
    return virtualStateParser.requestText;
  }

  public static RequestParser parserFor(final ByteBuffer requestContent) {
    return new RequestParser(requestContent);
  }

  public boolean hasCompleted() {
    return virtualStateParser.hasCompleted();
  }

  public Request fullRequest() {
    return virtualStateParser.fullRequest();
  }

  public boolean hasFullRequest() {
    return virtualStateParser.hasFullRequest();
  }

  public boolean hasMissingContentTimeExpired(final long timeLimit) {
    return virtualStateParser.hasMissingContentTimeExpired(timeLimit);
  }

  public boolean isMissingContent() {
    return virtualStateParser.isMissingContent();
  }

  public void parseNext(final ByteBuffer requestContent) {
    virtualStateParser.includes(requestContent).parse();
  }

  private static String header = "==========";

  @Override
  public String toString() {

    final StringBuilder builder = new StringBuilder();

    builder.append(header).append(" REQUEST PARSER CONTEXT:\n");

    builder.append(virtualStateParser.method.name).append(" ").append(virtualStateParser.uri).append("\n");

    for (final RequestHeader header : virtualStateParser.headers) {
      builder.append(header.name).append(": ").append(header.value).append("\n");
    }

    builder.append("\n").append(virtualStateParser.body != null ? virtualStateParser.body.content() : "");

    builder.append(header).append("\n").append("DANGLING:\n").append(virtualStateParser.requestText).append("\n");

    builder.append(header).append(" END\n");

    return builder.toString();
  }

  private RequestParser(final ByteBuffer requestContent) {
    this.virtualStateParser = new VirtualStateParser().includes(requestContent).parse();
  }

  //=========================================
  // VirtualStateParser
  //=========================================

  static class VirtualStateParser {

    private enum Step { NotStarted, RequestLine, Headers, Body, Completed };

    // DO NOT RESET: (1) contentQueue, (2) position, (3) requestText

    private final Queue<ContentPacket> contentQueue;
    private int position;
    private String requestText;

    // DO NOT RESET: (1) headers, (2) fullRequests

    private Body body;
    private int contentLength;
    private int contentExtraLength;
    private boolean continuation;
    private Step currentStep;
    private List<Request> fullRequests;
    private ListIterator<Request> fullRequestsIterator;
    private Headers<RequestHeader> headers;
    private Method method;
    private long outOfContentTime;
    private URI uri;
    private Version version;

    VirtualStateParser() {
      this.contentQueue = new LinkedList<>();
      this.currentStep = Step.NotStarted;
      this.requestText = "";
      this.headers = new Headers<>(2);
      this.fullRequests = new ArrayList<>(2);

      reset();
    }

    Request fullRequest() {
      if (fullRequestsIterator == null) {
        fullRequestsIterator = fullRequests.listIterator();
      }
      if (fullRequestsIterator.hasNext()) {
        final Request fullRequest = fullRequestsIterator.next();
        fullRequestsIterator.remove();
        return fullRequest;
      }
      throw new IllegalStateException(Response.Status.BadRequest + "\n\nRequest is not completed: " + method + " " + uri);
    }

    boolean hasFullRequest() {
      if (fullRequestsIterator != null) {
        if (!fullRequestsIterator.hasNext()) {
          fullRequestsIterator = null;
          return false;
        } else {
          return true;
        }
      }
      if (fullRequests.isEmpty()) {
        fullRequestsIterator = null;
        return false;
      }
      return true;
    }

    boolean hasCompleted() {
      if (isNotStarted() && position >= requestText.length() && contentQueue.isEmpty()) {
        requestText = compact();
        return true;
      }
      return false;
    }

    boolean hasMissingContentTimeExpired(final long timeLimit) {
      final long timeOutTime = outOfContentTime + timeLimit;
      return timeOutTime < System.currentTimeMillis();
    }

    VirtualStateParser includes(final ByteBuffer requestContent) {
      outOfContentTime = 0;
      final String requestContentText = Converters.bytesToText(requestContent.array(), 0, requestContent.limit());
      final int utf8ExtraLength = requestContent.remaining() - requestContentText.length();
      if (contentQueue.isEmpty()) {
        contentExtraLength += utf8ExtraLength;
        requestText = requestText.concat(requestContentText);
      } else {
        contentQueue.add(new ContentPacket(requestContentText, utf8ExtraLength));
      }
      return this;
    }

    boolean isMissingContent() {
      return outOfContentTime > 0;
    }

    VirtualStateParser parse() {
      boolean isOutOfContent = false;
      while (!hasCompleted()) {
        if (isNotStarted()) {
          isOutOfContent = nextStep();
        } else if (isRequestLineStep()) {
          isOutOfContent = parseRequestLine();
        } else if (isHeadersStep()) {
          isOutOfContent = parseHeaders();
        } else if (isBodyStep()) {
          isOutOfContent = parseBody();
        } else if (isCompletedStep()) {
          continuation = false;
          isOutOfContent = newRequest();
        }
        if (isOutOfContent) {
          continuation = true;
          outOfContentTime = System.currentTimeMillis();
          return this;
        }
      }
      return this;
    }

    private String compact() {
      final String compact = requestText.substring(position);
      position = 0;
      return compact;
    }

    private Optional<String> nextLine(final String errorResult, final String errorMessage) {
      int possibleCarriageReturnIndex = -1;
      final int lineBreak = requestText.indexOf("\n", position);
      if (lineBreak < 0) {
        if (contentQueue.isEmpty()) {
          requestText = compact();
          return Optional.empty();
        }
        final ContentPacket packet = contentQueue.poll();
        contentExtraLength += packet.utf8ExtraLength;
        requestText = compact().concat(packet.content);
        return nextLine(errorResult, errorMessage);
      } else if (lineBreak == 0) {
        possibleCarriageReturnIndex = 0;
      }
      final int endOfLine = requestText.charAt(lineBreak + possibleCarriageReturnIndex) == '\r' ? lineBreak - 1 : lineBreak;
      final String line  = requestText.substring(position, endOfLine).trim();
      position = lineBreak + 1;
      return Optional.of(line);
    }

    private boolean nextStep() {
      if (isNotStarted()) {
        currentStep = Step.RequestLine;
      } else if (isRequestLineStep()) {
        currentStep = Step.Headers;
      } else if (isHeadersStep()) {
        currentStep = Step.Body;
      } else if (isBodyStep()) {
        currentStep = Step.Completed;
      } else if (isCompletedStep()) {
        currentStep = Step.NotStarted;
      }
      return false;
    }

    private boolean isBodyStep() {
      return currentStep == Step.Body;
    }

    private boolean isCompletedStep() {
      return currentStep == Step.Completed;
    }

    private boolean isHeadersStep() {
      return currentStep == Step.Headers;
    }

    private boolean isNotStarted() {
      return currentStep == Step.NotStarted;
    }

    private boolean isRequestLineStep() {
      return currentStep == Step.RequestLine;
    }

    private boolean parseBody() {
      continuation = false;
      if (contentLength > 0) {
        final int endIndex = position + contentLength;
        if (requestText.length() + contentExtraLength < endIndex) {
          if (contentQueue.isEmpty()) {
            requestText = compact();
            return true;
          }
          final ContentPacket packet = contentQueue.poll();
          requestText = compact() + packet.content;
          contentExtraLength += packet.utf8ExtraLength;
          parseBody();
        } else {
          body = Body.from(requestText.substring(position, endIndex - contentExtraLength));
          position += (contentLength - contentExtraLength);
          nextStep();
        }
      } else {
        body = Body.from("");
        nextStep();
      }
      return false;
    }

    private boolean parseHeaders() {
      if (!continuation) {
        headers = new Headers<>(8);
      }
      continuation = false;
      while (true) {
        final Optional<String> maybeHeaderLine =
          nextLine(Response.Status.BadRequest.toString(), "\n\nHeader is required.");
        if (!maybeHeaderLine.isPresent()) {
          return true;
        }
        final String headerLine = maybeHeaderLine.get();
        if (headerLine.isEmpty()) {
          break;
        }
        final RequestHeader header = RequestHeader.from(headerLine);
        headers.add(header);
        if (contentLength == 0) {
          final int maybeContentLength = header.ifContentLength();
          if (maybeContentLength > 0) {
            contentLength = maybeContentLength;
          }
        }
      }
      if (headers.isEmpty()) {
        throw new IllegalArgumentException(Response.Status.BadRequest + "\n\nHeader is required.");
      }
      return nextStep();
    }

    private boolean parseRequestLine() {
      continuation = false;
      final Optional<String> maybeLine = nextLine(Response.Status.BadRequest.toString(), "\n\nRequest line is required.");
      if (!maybeLine.isPresent()) {
        return true;
      }
      final StringTokenizer tokenizer = new StringTokenizer(maybeLine.get(), " ");
      try {
        method = Method.from(parseNextRequestLinePart(tokenizer, "Method"));
        uri = new URI(parseNextRequestLinePart(tokenizer, "URI/path"));
        version = Version.from(parseNextRequestLinePart(tokenizer, "HTTP/version"));

        return nextStep();
      } catch (Exception e) {
        throw new IllegalArgumentException(Response.Status.BadRequest.toString() + "\n\nParsing exception: " + e.getMessage(), e);
      }
    }

    private String parseNextRequestLinePart(final StringTokenizer tokenizer, final String expectedPartName) {
      if(tokenizer.hasMoreTokens()) {
        return tokenizer.nextToken();
      }
      throw new IllegalArgumentException(Response.Status.BadRequest + "\n\nRequest line part missing: " + expectedPartName);
    }

    private boolean newRequest() {
      final Request request = new Request(method, uri, version, headers, body);
      fullRequests.add(request);
      reset();
      return nextStep();
    }

    private void reset() {
      // DO NOT RESET: (1) contentQueue, (2) position, (3) requestText, (4) headers, (5) fullRequests

      this.body = null;
      this.contentLength = 0;
      this.contentExtraLength = 0;
      this.continuation = false;
      this.method = null;
      this.outOfContentTime = 0;
      this.version = null;
      this.uri = null;
    }
  }
}
