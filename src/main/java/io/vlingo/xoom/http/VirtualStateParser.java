package io.vlingo.xoom.http;

import io.vlingo.xoom.wire.message.Converters;

import java.nio.ByteBuffer;
import java.util.*;

class Responses{
  protected List<Response> fullResponses;
  protected ListIterator<Response> fullResponsesIterator;
  protected Header.Headers<ResponseHeader> headers;
  protected Response.Status status;
  protected String responseText;
  protected int currentResponseTextLength;

  Response fullResponse() {
    if (fullResponsesIterator == null) {
      fullResponsesIterator = fullResponses.listIterator();
    }
    if (fullResponsesIterator.hasNext()) {
      final Response fullResponse = fullResponsesIterator.next();
      fullResponsesIterator.remove();
      return fullResponse;
    }
    fullResponsesIterator = null;
    throw new IllegalStateException("Response is not completed.");
  }

  boolean hasFullResponse() {
    if (fullResponsesIterator != null) {
      if (!fullResponsesIterator.hasNext()) {
        fullResponsesIterator = null;
        return false;
      } else {
        return true;
      }
    }
    if (fullResponses.isEmpty()) {
      fullResponsesIterator = null;
      return false;
    }
    return true;
  }

}

//=========================================
// VirtualStateParser
//=========================================
public class VirtualStateParser extends  Responses{
  private static class OutOfContentException extends RuntimeException { private static final long serialVersionUID = 1L; }

  private enum Step { NotStarted, StatusLine, Headers, Body, Completed };

  // DO NOT RESET: (1) contentQueue, (2) position, (3) requestText (4) currentResponseTextLength

  private final Queue<ContentPacket> contentQueue;
  private int position;

  private boolean transferEncodingChunked;

  // DO NOT RESET: (1) headers, (2) fullResponses

  private Body body;
  private boolean bodyOnly;
  private int contentLength;
  private int contentExtraLength;
  private boolean continuation;
  private Step currentStep;
  private boolean keepAlive;
  private long outOfContentTime;
  private boolean stream;
  private Version version;

  VirtualStateParser() {
    this(false);
  }

  VirtualStateParser(final boolean bodyOnly) {
    this.bodyOnly = bodyOnly;
    this.contentQueue = new LinkedList<>();
    this.currentStep = Step.NotStarted;
    this.responseText = "";
    this.headers = new Header.Headers<>(2);
    this.fullResponses = new ArrayList<>(2);

    this.keepAlive = false;
    this.stream = false;

    reset();
  }

  boolean hasCompleted() {
    if (isNotStarted() && position >= currentResponseTextLength && contentQueue.isEmpty()) {
      responseText = compact();
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
    final int utf8ExtraLength = responseContent.remaining() - responseContentText.length();
    if (contentQueue.isEmpty()) {
      contentExtraLength += utf8ExtraLength;
      responseText = responseText + responseContentText;
      currentResponseTextLength = responseText.length();
    } else {
      contentQueue.add(new ContentPacket(responseContentText, utf8ExtraLength));
    }
    return this;
  }

  boolean isKeepAliveConnection() {
    return keepAlive;
  }

  boolean isMissingContent() {
    return outOfContentTime > 0;
  }

  boolean isStreamContentType() {
    return stream;
  }

  VirtualStateParser parse() {
    while (!hasCompleted()) {
      try {
        if (isNotStarted()) {
//            System.out.println("NOT STARTED: ");
          nextStep();
        } else if (isStatusLineStep()) {
//            System.out.println("STATUS LINE");
          parseStatusLine();
        } else if (isHeadersStep()) {
//            System.out.println("HEADERS");
          parseHeaders();
        } else if (isBodyStep()) {
//            System.out.println("BODY");
          parseBody();
//            System.out.println("BODY: " + body);
        } else if (isCompletedStep()) {
//            System.out.println("COMPLETED: REMAINING: " + responseText.substring(position));
          continuation = false;
          newResponse();
        }
      } catch (OutOfContentException e) {
        continuation = true;
        outOfContentTime = System.currentTimeMillis();
        return this;
      } catch (Throwable t) {
        throw t;
      }
    }

    prepareForStream();

    return this;
  }

  private void clearContent() {
    responseText = "";
    currentResponseTextLength = 0;
    position = 0;
  }

  private String compact() {
    final String compact = responseText.substring(position);
    position = 0;
    currentResponseTextLength = compact.length();
    return compact;
  }

  private String nextLine(final boolean mayBeBlank, final String errorMessage) {
    int possibleCarriageReturnIndex = -1;
    final int lineBreak = responseText.indexOf("\n", position);
    if (lineBreak < 0) {
      if (contentQueue.isEmpty()) {
        responseText = compact();
        throw new OutOfContentException();
      }
      final ContentPacket packet = contentQueue.poll();
      contentExtraLength += packet.utf8ExtraLength;
      responseText = compact() + packet.content;
      return nextLine(mayBeBlank, errorMessage);
    } else if (lineBreak == 0) {
      possibleCarriageReturnIndex = 0;
    }
    final int endOfLine = responseText.charAt(lineBreak + possibleCarriageReturnIndex) == '\r' ? lineBreak - 1 : lineBreak;
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
    if (bodyOnly) {
      contentLength = responseText.length();
    }

    continuation = false;
    if (contentLength > 0) {
      final int endIndex = position + contentLength;
      if (currentResponseTextLength + contentExtraLength < endIndex) {
        if (contentQueue.isEmpty()) {
          responseText = compact();
          throw new OutOfContentException();
        }
        final ContentPacket packet = contentQueue.poll();
        responseText = compact() + packet.content;
        contentExtraLength += packet.utf8ExtraLength;
        parseBody();
        return;
      }
      body = Body.from(responseText.substring(position, endIndex - contentExtraLength));
      position += (contentLength - contentExtraLength);
    } else if (transferEncodingChunked) {
      body = Body.from(parseChunks());
    } else {
      body = Body.empty();
    }
    nextStep();
  }

  private String parseBodyChunk(final int chunkLength) {
    final int endIndex = position + chunkLength;
    if (currentResponseTextLength < endIndex) {
      if (contentQueue.isEmpty()) {
        responseText = compact();
        throw new OutOfContentException();
      }
      responseText = compact() + contentQueue.poll();
    }
    final String chunk = responseText.substring(position, endIndex);
    position += chunk.length();
//      System.out.println("CHUNK: " + chunk);
    return chunk;
  }

  private String parseChunks() {
    final StringBuilder builder = new StringBuilder();

    int chunkLength = chunkLength();
//      System.out.println("CHUNK LENGTH: " + chunkLength);
    while (chunkLength > 0) {
      final String chunk = parseBodyChunk(chunkLength);
      builder.append(chunk);
      chunkLength = chunkLength();
//        System.out.println("CHUNK LENGTH: " + chunkLength);
    }

    clearContent();

    return builder.toString();
  }

  private void parseHeaders() {
    if (bodyOnly) {
      nextStep();
      return;
    }
    if (!continuation) {
      headers = new Header.Headers<>(2);
    }
    continuation = false;
    while (true) {
      final String maybeHeaderLine = nextLine(true, null);
      if (maybeHeaderLine.isEmpty()) {
        break;
      }
      final ResponseHeader header = ResponseHeader.from(maybeHeaderLine);
      headers.add(header);
      if (contentLength == 0) {
        final int maybeContentLength = header.ifContentLength();
        if (maybeContentLength >= 0) {
          contentLength = maybeContentLength;
        } else if (header.isTransferEncodingChunked()) {
          transferEncodingChunked = true;
        }
      }
      if (!keepAlive && header.isKeepAliveConnection()) {
        this.keepAlive = true;
      } else if (!stream && header.isStreamContentType()) {
        this.stream = true;
      }
    }
    nextStep();
  }

  private void parseStatusLine() {
    if (bodyOnly) {
      version = Version.Http1_1;
      status = Response.Status.Ok;
      nextStep();
      return;
    }

    continuation = false;
    final String line = nextLine(false, "Response status line is required.");
    final int spaceIndex = line.indexOf(' ');

    try {
      version = Version.from(line.substring(0, spaceIndex).trim());
      status = Response.Status.valueOfRawState(line.substring(spaceIndex + 1).trim());

      nextStep();
    } catch (Throwable e) {
      throw new IllegalArgumentException("Response status line parsing exception: " + e.getMessage(), e);
    }
  }

  private void prepareForStream() {
    if (!bodyOnly) {
      if (keepAlive && stream) {
        bodyOnly = true;
      }
    }
  }

  private void newResponse() {
    final Response response = Response.of(version, status, headers, body);
    fullResponses.add(response);
    reset();
    nextStep();
  }

  private void reset() {
    // DO NOT RESET: (1) contentQueue, (2) position, (3) responseText, (4) headers, (5) fullResponses

    this.body = null;
    this.contentLength = 0;
    this.contentExtraLength = 0;
    this.continuation = false;
    this.outOfContentTime = 0;
    this.status = null;
    this.version = null;
    this.transferEncodingChunked = false;
  }

  private int chunkLength() {
    try {
      String line = nextLine(false, "Missing chunk length.");
      if (line.isEmpty())  {
        line = nextLine(false, "Missing chunk length.");
      }
//        System.out.println("CHUNK LENGTH TEXT: " + line);
      return Integer.parseInt(line, 16);
    } catch (Exception e) {
      return 0;
    }
  }
}
