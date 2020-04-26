// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import java.util.function.Function;

import io.vlingo.http.Header.Headers;
import io.vlingo.wire.message.ConsumerByteBuffer;
import io.vlingo.wire.message.Converters;

/**
 * A response to an HTTP request, including headers, entity/body. Factory methods
 * are provided for fluent creation, as well as an enumeration of statuses.
 */
public class Response {

  public static Response of(final Status statusCode) {
    return new Response(Version.Http1_1, statusCode, Headers.empty(), Body.from(""));
  }

  public static Response of(final Version version, final Status statusCode) {
    return new Response(version, statusCode, Headers.empty(), Body.from(""));
  }

  public static Response of(final Status statusCode, final String entity) {
    return new Response(Version.Http1_1, statusCode, Headers.empty(), Body.from(entity));
  }

  public static Response of(final Status statusCode, final byte[] entity) {
    return new Response(Version.Http1_1, statusCode, Headers.empty(), Body.from(entity));
  }

  public static Response of(final Version version, final Status statusCode, final String entity) {
    return new Response(version, statusCode, Headers.empty(), Body.from(entity));
  }

  public static Response of(final Version version, final Status statusCode, final byte[] entity) {
    return new Response(version, statusCode, Headers.empty(), Body.from(entity));
  }

  public static Response of(final Status statusCode, final Headers<ResponseHeader> headers) {
    return new Response(Version.Http1_1, statusCode, headers, Body.from(""));
  }

  public static Response of(final Version version, final Status statusCode, final Headers<ResponseHeader> headers) {
    return new Response(version, statusCode, headers, Body.from(""));
  }

  public static Response of(final Status statusCode, final Headers<ResponseHeader> headers, final String entity) {
    return new Response(Version.Http1_1, statusCode, headers, Body.from(entity));
  }

  public static Response of(final Status statusCode, final Headers<ResponseHeader> headers, final byte[] entity) {
    return new Response(Version.Http1_1, statusCode, headers, Body.from(entity));
  }

  public static Response of(final Version version, final Status statusCode, final Headers<ResponseHeader> headers, final String entity) {
    return new Response(version, statusCode, headers, Body.from(entity));
  }

  public static Response of(final Version version, final Status statusCode, final Headers<ResponseHeader> headers, final byte[] entity) {
    return new Response(version, statusCode, headers, Body.from(entity));
  }

  public static Response of(final Status statusCode, final Body body) {
    return new Response(Version.Http1_1, statusCode, Headers.empty(), body);
  }

  public static Response of(final Status statusCode, final Headers<ResponseHeader> headers, final Body entity) {
    return new Response(Version.Http1_1, statusCode, headers, entity);
  }

  public static Response of(final Version version, final Status statusCode, final Headers<ResponseHeader> headers, final Body entity) {
    return new Response(version, statusCode, headers, entity);
  }

  public final Status status;
  public final String statusCode;
  public final Headers<ResponseHeader> headers;
  public final Body entity;
  public final Version version;

  public Header headerOf(final String name) {
    for (final Header header : headers) {
      if (header.name.equalsIgnoreCase(name)) {
        return header;
      }
    }
    return null;
  }

  public boolean headerMatches(final String name, final String value) {
    final Header header = headerOf(name);
    return header == null ? false : header.matchesValueOf(value);
  }

  public String headerValueOr(final String headerName, final String defaultValue) {
    final Header header = headerOf(headerName);
    return header == null ? defaultValue : header.value;
  }

  public Response include(final Header header) {
    if (header != null && headerOf(header.name) == null) {
      headers.and(ResponseHeader.of(header.name, header.value));
    }
    return this;
  }

  public ConsumerByteBuffer into(final ConsumerByteBuffer buffer) {
    Function<String, byte[]> convert = Converters::textToBytes;
    Function<byte[], ConsumerByteBuffer> put = buffer::put;
    return into(convert.andThen(put)).flip();
  }

  public int size() {
    int headersSize = 0;
    for (final ResponseHeader header : headers) {
      // name + ": " + value + "\n"
      headersSize += (header.name.length() + 2 + header.value.length() + 1);
    }
    // HTTP/1.1 + 1 + status + "\n" + headers + "\n" + entity + just-in-case
    return 9 + status.toString().length() + 1 + headersSize + 1 + entity.content().length() + 5;
  }

  private <R> R into(Function<String,R> appender) {
    // TODO: currently supports only HTTP/1.1
    appender.apply(Version.HTTP_1_1);
    appender.apply(" ");
    appender.apply(status.toString());
    appender.apply("\n");

    appendAllHeadersTo(appender);
    appender.apply("\n");
    return appender.apply(entity.toString());
  }


  @Override
  public String toString() {
    return into(new StringBuilder(size())::append).toString();
  }

  protected Response(final Version version, final Status status, final Headers<ResponseHeader> headers, final Body entity) {
    this.version = version;
    this.status = status;
    this.statusCode = String.valueOf(status.code);
    this.entity = entityFrom(headers, entity);
    this.headers = addMissingContentLengthHeader(headers);
  }

  private Headers<ResponseHeader> addMissingContentLengthHeader(final Headers<ResponseHeader> headers) {
    if (!entity.isComplex()) {
      final int contentLength = entity.content().length();
      final Header header = headers.headerOf(ResponseHeader.ContentLength);
      if (header == null && contentLength > 0) {
        headers.add(ResponseHeader.of(ResponseHeader.ContentLength, Integer.toString(contentLength)));
      }
    }
    return headers;
  }

  private <R> void appendAllHeadersTo(Function<String, R> appender) {
    for (final ResponseHeader header : headers) {
      appender.apply(header.name);
      appender.apply(": ");
      appender.apply(header.value);
      appender.apply("\n");
    }
  }

  private Body entityFrom(final Headers<ResponseHeader> headers, final Body entity) {
    final Header header = headers.headerOf(ResponseHeader.TransferEncoding);

    if (header != null && header.value.equals("chunked")) {
      if (entity.isComplex() && !entity.hasContent()) {
        return Body.beginChunked();
      }
    }

    return entity;
  }

  public enum Status {
    // 1xx Informational responses
    Continue(100, "Continue"),
    SwitchingProtocols(101, "Switching Protocols"),
    Processing(102, "Processing"),
    EarlyHints (103, "Early Hints"),

    // 2xx Success
    Ok(200, "OK"),
    Created(201, "Created"),
    Accepted(202, "Accepted"),
    NonAuthoritativeInformation(203, "Non-Authoritative Information"),
    NoContent(204, "No Content"),
    ResetContent(205, "Reset Content"),
    PartialContent(206, "Partial Content"),
    MultiStatus(207, "Multi-Status"),
    AlreadyReported(208, "Already Reported"),
    IMUsed(226, "IM Used"),

    // 3xx Redirection
    MultipleChoices(300, "Multiple Choices"),
    MovedPermanently(301, "Moved Permanently"),
    Found(302, "Found"),
    SeeOther(303, "See Other"),
    NotModified(304, "Not Modified"),
    UseProxy(305, "Use Proxy"),
    SwitchProxy(306, "Switch Proxy"),
    TemporaryRedirect(307, "Temporary Redirect"),
    PermanentRedirect(308, "Permanent Redirect"),

    // 4xx Client errors
    BadRequest(400, "Bad Request"),
    Unauthorized(401, "Unauthorized"),
    PaymentRequired(402, "Payment Required"),
    Forbidden(403, "Forbidden"),
    NotFound(404, "Not Found"),
    MethodNotAllowed(405, "Method Not Allowed"),
    NotAcceptable(406, "Not Acceptable"),
    ProxyAuthenticationRequired(407, "Proxy Authentication Required"),
    RequestTimeout(408, "Request Timeout"),
    Conflict(409, "Conflict"),
    Gone(410, "Gone"),
    LengthRequired(411, "Length Required"),
    PreconditionFailed(412, "Precondition Failed"),
    PayloadTooLarge(413, "Payload Too Large"),
    URITooLong(414, "URI Too Long"),
    UnsupportedMediaType(415, "Unsupported Media Type"),
    RangeNotSatisfiable(416, "Range Not Satisfiable"),
    ExpectationFailed(417, "Expectation Failed"),
    Imateapot(418, "I'm a teapot"),
    MisdirectedRequest(421, "Misdirected Request"),
    UnprocessableEntity(422, "Unprocessable Entity"),
    Locked(423, "Locked"),
    FailedDependency(424, "Failed Dependency"),
    UpgradeRequired(426, "Upgrade Required"),
    PreconditionRequired(428, "Precondition Required"),
    TooManyRequests(429, "Too Many Requests"),
    RequestHeaderFieldsTooLarge(431, "Request Header Fields Too Large"),
    UnavailableForLegalReasons(451, "Unavailable For Legal Reasons"),

    // 5xx Server errors
    InternalServerError(500, "Internal Server Error"),
    NotImplemented(501, "Not Implemented"),
    BadGateway(502, "Bad Gateway"),
    ServiceUnavailable(503, "Service Unavailable"),
    GatewayTimeout(504, "Gateway Timeout"),
    HTTPVersionNotSupported(505, "HTTP Version Not Supported"),
    VariantAlsoNegotiates(506, "Variant Also Negotiates"),
    InsufficientStorage(507, "Insufficient Storage"),
    LoopDetected(508, "Loop Detected"),
    NotExtended(510, "Not Extended"),
    NetworkAuthenticationRequired(511, "Network Authentication Required");

    public final int code;
    public final String reason;
    Status(final int code, final String reason) {
      this.code = code;
      this.reason = reason;
    }

    @Override
    public String toString() {
      return new StringBuilder().append(code).append(' ').append(reason).toString();
    }

    public static Status valueOfRawState(final String value) {
      for(Status status: Status.values()) {
        if (status.toString().toLowerCase().equals(value.toLowerCase())) {
          return status;
        }
      }
      throw new IllegalArgumentException("status " + value + " is not valid");
    }
  }
}
