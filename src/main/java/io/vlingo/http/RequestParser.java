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
import java.util.ListIterator;
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
      if (contentQueue.isEmpty()) {
        requestText = requestText + requestContentText;
      } else {
        contentQueue.add(requestContentText);
      }
      return this;
    }

    boolean isMissingContent() {
      return outOfContentTime > 0;
    }

    VirtualStateParser parse() {
      while (!hasCompleted()) {
        try {
          if (isNotStarted()) {
            nextStep();
          } else if (isRequestLineStep()) {
            parseRequestLine();
          } else if (isHeadersStep()) {
            parseHeaders();
          } else if (isBodyStep()) {
            parseBody();
          } else if (isCompletedStep()) {
            continuation = false;
            newRequest();
          }
        } catch (OutOfContentException e) {
          continuation = true;
          outOfContentTime = System.currentTimeMillis();
          return this;
        } catch (Throwable t) {
          throw t;
        }
      }
      return this;
    }

    private String compact() {
      final String compact = requestText.substring(position);
      position = 0;
      return compact;
    }

    private String nextLine(final String errorResult, final String errorMessage) {
      int possibleCarriageReturnIndex = -1;
      final int lineBreak = requestText.indexOf("\n", position);
      if (lineBreak < 0) {
        if (contentQueue.isEmpty()) {
          requestText = compact();
          throw new OutOfContentException();
        }
        requestText = compact() + contentQueue.poll();
        return nextLine(errorResult, errorMessage);
      } else if (lineBreak == 0) {
        possibleCarriageReturnIndex = 0;
      }
      final int endOfLine = requestText.charAt(lineBreak + possibleCarriageReturnIndex) == '\r' ? lineBreak - 1 : lineBreak;
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

    private void parseBody() {
      continuation = false;
      if (contentLength > 0) {
        final int endIndex = position + contentLength;
        if (requestText.length() < endIndex) {
          if (contentQueue.isEmpty()) {
            requestText = compact();
            throw new OutOfContentException();
          }
          requestText = compact() + contentQueue.poll();
          parseBody();
        } else {
          body = Body.from(requestText.substring(position, endIndex));
          position += contentLength;
          nextStep();
        }
      } else {
        body = Body.from("");
        nextStep();
      }
    }

    private void parseHeaders() {
      if (!continuation) {
        headers.clear();
      }
      continuation = false;
      while (true) {
        final String maybeHeaderLine = nextLine(Response.Status.BadRequest.toString(), "\n\nHeader is required.");
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
        throw new IllegalArgumentException(Response.Status.BadRequest + "\n\nHeader is required.");
      }
      nextStep();
    }

    private void parseRequestLine() {
      continuation = false;
      final String line = nextLine(Response.Status.BadRequest.toString(), "\n\nRequest line is required.");
      final String[] parts = line.split(" ");

      try {
        method = Method.from(parseSpecificRequestLinePart(parts, 1, "Method"));
        uri = new URI(parseSpecificRequestLinePart(parts, 2, "URI/path"));
        version = Version.from(parseSpecificRequestLinePart(parts, 3, "HTTP/version"));
        
        nextStep();
      } catch (Exception e) {
        throw new IllegalArgumentException(Response.Status.BadRequest.toString() + "\n\nParsing exception: " + e.getMessage(), e);
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
      throw new IllegalArgumentException(Response.Status.BadRequest + "\n\nRequest line part missing: " + name);
    }

    private void newRequest() {
      final Request request = new Request(method, uri, version, headers, body);
      fullRequests.add(request);
      reset();
      nextStep();
    }

    private void reset() {
      // DO NOT RESET: (1) contentQueue, (2) position, (3) requestText, (4) headers, (5) fullRequests

      this.body = null;
      this.contentLength = 0;
      this.continuation = false;
      this.method = null;
      this.outOfContentTime = 0;
      this.version = null;
      this.uri = null;
    }
  }
}
