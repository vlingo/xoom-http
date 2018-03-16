// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import io.vlingo.http.Header.Headers;
import io.vlingo.wire.message.Converters;

public class ResponseParser {
  private final VirtualStateParser virtualStateParser;

  public static ResponseParser parserFor(final ByteBuffer requestContent) {
    return new ResponseParser(requestContent);
  }

  public boolean hasCompleted() {
    return virtualStateParser.hasCompleted();
  }

  public Response fullResponse() {
    return virtualStateParser.fullResponse();
  }

  public boolean hasFullResponse() {
    return !virtualStateParser.fullResponses.isEmpty();
  }

  public boolean hasMissingContentTimeExpired(final long timeLimit) {
    return virtualStateParser.hasMissingContentTimeExpired(timeLimit);
  }

  public boolean isMissingContent() {
    return virtualStateParser.isMissingContent();
  }

  public void parseNext(final ByteBuffer responseContent) {
    virtualStateParser.includes(responseContent).parse();
  }

  private ResponseParser(final ByteBuffer responseContent) {
    this.virtualStateParser = new VirtualStateParser().includes(responseContent).parse();
  }

  //=========================================
  // VirtualStateParser
  //=========================================

  static class VirtualStateParser {
    private static class OutOfContentException extends RuntimeException { private static final long serialVersionUID = 1L; }

    private enum Step { NotStarted, StatusLine, Headers, Body, Completed };

    // DO NOT RESET: (1) contentQueue, (2) position, (3) requestText

    private final Queue<String> contentQueue;
    private int position;
    private String responseText;

    // DO NOT RESET: (1) headers, (2) fullResponses

    private Body body;
    private int contentLength;
    private Step currentStep;
    private List<Response> fullResponses;
    private Headers<ResponseHeader> headers;
    private long outOfContentTime;
    private String status;
    private Version version;

    VirtualStateParser() {
      this.contentQueue = new LinkedList<>();
      this.responseText = "";
      this.headers = new Headers<>(2);
      this.fullResponses = new ArrayList<>(2);

      reset();
    }

    Response fullResponse() {
      if (!fullResponses.isEmpty()) {
        final Response fullResponseHolder = fullResponses.remove(0);
        reset();
        return fullResponseHolder;
      }
      throw new IllegalStateException("Response is not completed.");
    }

    boolean hasCompleted() {
      if (isNotStarted() && position >= responseText.length() && contentQueue.isEmpty()) {
        responseText = responseText.substring(position, responseText.length());
        position = 0;
        return true;
      }
      return false;
    }

    boolean hasMissingContentTimeExpired(final long timeLimit) {
      final long timeOutTime = outOfContentTime + timeLimit;
      return timeOutTime < System.currentTimeMillis();
    }

    VirtualStateParser includes(final ByteBuffer responseContent) {
      outOfContentTime = 0;
      final String responseContentText = Converters.bytesToText(responseContent.array(), 0, responseContent.limit());
      contentQueue.add(responseContentText);
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
            } else if (isStatusLineStep()) {
              parseStatusLine();
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
          final Response response = Response.of(version, status, headers, body);
          fullResponses.add(response);
          restart();
        }
      }
      return this;
    }

    private String nextLine(final String errorMessage) {
      final int lineBreak = responseText.indexOf("\n", position);
      if (lineBreak <= 0) {
        if (errorMessage == null) {
          return "";
        }
        if (contentQueue.isEmpty()) {
          throw new OutOfContentException();
        }
        responseText = responseText.substring(position) + contentQueue.poll();
        position = 0;
        return nextLine(errorMessage);
      }
      final int endOfLine = responseText.charAt(lineBreak - 1) == '\r' ? lineBreak - 1 : lineBreak;
      final String line  = responseText.substring(position, endOfLine).trim();
      position = lineBreak + 1;
      return line;
    }

    private void nextStep() {
      if (isNotStarted()) {
        currentStep = Step.StatusLine;
      } else if (isStatusLineStep()) {
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

    private boolean isStatusLineStep() {
      return currentStep == Step.StatusLine;
    }

    private void parseBody() {
      if (contentLength > 0) {
        final int endIndex = position + contentLength;
        if (responseText.length() < endIndex) {
          if (contentQueue.isEmpty()) {
            return;
          }
          responseText = responseText.substring(position) + contentQueue.poll();
          position = 0;
          parseBody();
        }
        body = Body.from(responseText.substring(position, endIndex));
        position = endIndex;
      } else {
        body = Body.from("");
      }
      nextStep();
    }

    private void parseHeaders() {
      headers.clear();
      while (true) {
        final String maybeHeaderLine = nextLine(null);
        if (maybeHeaderLine.isEmpty()) {
          break;
        }
        final ResponseHeader header = ResponseHeader.from(maybeHeaderLine);
        headers.add(header);
        if (contentLength == 0) {
          final int maybeContentLength = header.ifContentLength();
          if (maybeContentLength > 0) {
            contentLength = maybeContentLength;
          }
        }
      }
      nextStep();
    }

    private void parseStatusLine() {
      final String line = nextLine("Response status line is required.");
      final String[] parts = line.split(" ");

      try {
        version = Version.from(parseSpecificStatusLinePart(parts, 1, "HTTP/version"));
        status = parseSpecificStatusLinePart(parts, 2, "Status");
        
        nextStep();
      } catch (Exception e) {
        throw new IllegalArgumentException("Response status line parsing exception: " + e.getMessage(), e);
      }
    }

    private String parseSpecificStatusLinePart(final String[] parts, final int part, final String name) {
      int partCount = 0;
      for (int idx = 0; idx < parts.length; ++idx) {
        if (parts[idx].length() > 0) {
          if (++partCount == part) {
            return parts[idx];
          }
        }
      }
      throw new IllegalArgumentException("Response line part missing: " + name);
    }

    private void reset() {
      // DO NOT RESET: (1) contentQueue, (2) position, (3) responseText, (4) headers, (5) fullResponses

      this.body = null;
      this.contentLength = 0;
      this.currentStep = Step.NotStarted;
      this.outOfContentTime = 0;
      this.status = null;
      this.version = null;
    }

    private void restart() {
      this.currentStep = Step.NotStarted;
    }
  }
}
