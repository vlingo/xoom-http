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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import io.vlingo.http.Header.Headers;
import io.vlingo.wire.message.Converters;

public class RequestParser {
  private final VirtualStateParser virtualStateParser;

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
    return !virtualStateParser.fullRequests.isEmpty();
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

  private RequestParser(final ByteBuffer requestContent) {
    this.virtualStateParser = new VirtualStateParser().includes(requestContent).parse();
  }

  //=========================================
  // VirtualStateParser
  //=========================================

  static class VirtualStateParser {
    private static class OutOfContentException extends RuntimeException { private static final long serialVersionUID = 1L; }

    private enum Step { NotStarted, RequestLine, Headers, Body, Completed };

    // DO NOT RESET: (1) contentQueue, (2) position, (3) requestText

    private final Queue<String> contentQueue;
    private int position;
    private String requestText;

    // DO NOT RESET: (1) headers, (2) fullRequests

    private Body body;
    private int contentLength;
    private Step currentStep;
    private List<Request> fullRequests;
    private Headers<RequestHeader> headers;
    private Method method;
    private long outOfContentTime;
    private URI uri;
    private Version version;

    VirtualStateParser() {
      this.contentQueue = new LinkedList<>();
      this.requestText = "";
      this.headers = new Headers<>(2);
      this.fullRequests = new ArrayList<>(2);

      reset();
    }

    Request fullRequest() {
      if (!fullRequests.isEmpty()) {
        final Request fullRequestHolder = fullRequests.remove(0);
        reset();
        return fullRequestHolder;
      }
      throw new IllegalStateException(Response.BadRequest + "\n\nRequest is not completed: " + method + " " + uri);
    }

    boolean hasCompleted() {
      if (isNotStarted() && position >= requestText.length() && contentQueue.isEmpty()) {
        requestText = requestText.substring(position, requestText.length());
        position = 0;
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
      contentQueue.add(requestContentText);
      return this;
    }

    boolean isMissingContent() {
      return outOfContentTime > 0;
    }

    VirtualStateParser parse() {
      while (!hasCompleted()) {
        try {
          while (!isCompletedStep()) {
            if (isNotStarted()) {
              nextStep();
            } else if (isRequestLineStep()) {
              parseRequestLine();
            } else if (isHeadersStep()) {
              parseHeaders();
            } else if (isBodyStep()) {
              parseBody();
            }
          }
        } catch (OutOfContentException e) {
          outOfContentTime = System.currentTimeMillis();
          restart();
          return this;
        } catch (Throwable t) {
          throw t;
        }
  
        if (isCompletedStep()) {
          final Request request = new Request(method, uri, version, headers, body);
          fullRequests.add(request);
          restart();
        }
      }
      return this;
    }

    private String nextLine(final String errorResult, final String errorMessage) {
      final int lineBreak = requestText.indexOf("\n", position);
      if (lineBreak <= 0) {
        if (contentQueue.isEmpty()) {
          throw new OutOfContentException();
        }
        requestText = requestText.substring(position) + contentQueue.poll();
        position = 0;
        return nextLine(errorResult, errorMessage);
      }
      final int endOfLine = requestText.charAt(lineBreak - 1) == '\r' ? lineBreak - 1 : lineBreak;
      final String line  = requestText.substring(position, endOfLine).trim();
      position = lineBreak + 1;
      return line;
    }

    private void nextStep() {
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
    }

    private boolean isCompletedStep() {
      return currentStep == Step.Completed;
    }

    private boolean isHeadersStep() {
      return currentStep == Step.Headers;
    }

    private boolean isBodyStep() {
      return currentStep == Step.Body;
    }

    private boolean isNotStarted() {
      return currentStep == Step.NotStarted;
    }

    private boolean isRequestLineStep() {
      return currentStep == Step.RequestLine;
    }

    private void parseBody() {
      if (contentLength > 0) {
        final int endIndex = position + contentLength;
        if (requestText.length() < endIndex) {
          if (contentQueue.isEmpty()) {
            return;
          }
          requestText = requestText.substring(position) + contentQueue.poll();
          position = 0;
          parseBody();
        }
        body = Body.from(requestText.substring(position, endIndex));
        position = endIndex;
      } else {
        body = Body.from("");
      }
      nextStep();
    }

    private void parseHeaders() {
      headers.clear();
      while (true) {
        final String maybeHeaderLine = nextLine(Response.BadRequest, "\n\nHeader is required.");
        if (maybeHeaderLine.isEmpty()) {
          break;
        }
        final RequestHeader header = RequestHeader.from(maybeHeaderLine);
        headers.add(header);
        if (contentLength == 0) {
          final int maybeContentLength = header.ifContentLength();
          if (maybeContentLength > 0) {
            contentLength = maybeContentLength;
          }
        }
      }
      if (headers.isEmpty()) {
        throw new IllegalArgumentException(Response.BadRequest + "\n\nHeader is required.");
      }
      nextStep();
    }

    private void parseRequestLine() {
      final String line = nextLine(Response.BadRequest, "\n\nRequest line is required.");
      final String[] parts = line.split(" ");

      try {
        method = Method.from(parseSpecificRequestLinePart(parts, 1, "Method"));
        uri = new URI(parseSpecificRequestLinePart(parts, 2, "URI/path"));
        version = Version.from(parseSpecificRequestLinePart(parts, 3, "HTTP/version"));
        
        nextStep();
      } catch (Exception e) {
        throw new IllegalArgumentException(Response.BadRequest + "\n\nParsing exception: " + e.getMessage(), e);
      }
    }

    private String parseSpecificRequestLinePart(final String[] parts, final int part, final String name) {
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

    private void reset() {
      // DO NOT RESET: (1) contentQueue, (2) position, (3) requestText, (4) headers, (5) fullRequests

      this.body = null;
      this.contentLength = 0;
      this.currentStep = Step.NotStarted;
      this.method = null;
      this.outOfContentTime = 0;
      this.version = null;
      this.uri = null;
    }

    private void restart() {
      this.currentStep = Step.NotStarted;
    }
  }
}
