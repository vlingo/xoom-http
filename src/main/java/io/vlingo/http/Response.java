// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import io.vlingo.http.Header.Headers;
import io.vlingo.wire.message.ConsumerByteBuffer;
import io.vlingo.wire.message.Converters;

public class Response {
  // 1xx Informational responses
  public static final String Continue = "100 Continue";
  public static final String SwitchingProtocols = "101 Switching Protocols";
  public static final String Processing = "102 Processing";
  public static final String EarlyHints = "103 Early Hints";

  // 2xx Success
  public static final String Ok = "200 OK";
  public static final String Created = "201 Created";
  public static final String Accepted = "202 Accepted";
  public static final String NonAuthoritativeInformation = "203 Non-Authoritative Information";
  public static final String NoContent = "204 No Content";
  public static final String ResetContent = "205 Reset Content";
  public static final String PartialContent = "206 Partial Content";
  public static final String MultiStatus = "207 Multi-Status";
  public static final String AlreadyReported = "208 Already Reported";
  public static final String IMUsed = "226 IM Used";
  
  // 3xx Redirection
  public static final String MultipleChoices = "300 Multiple Choices";
  public static final String MovedPermanently = "301 Moved Permanently";
  public static final String Found = "302 Found";
  public static final String SeeOther = "303 See Other";
  public static final String NotModified = "304 Not Modified";
  public static final String UseProxy = "305 Use Proxy";
  public static final String SwitchProxy = "306 Switch Proxy";
  public static final String TemporaryRedirect = "307 Temporary Redirect";
  public static final String PermanentRedirect = "308 Permanent Redirect";
  
  // 4xx Client errors
  public static final String BadRequest = "400 Bad Request";
  public static final String Unauthorized = "401 Unauthorized";
  public static final String PaymentRequired = "402 Payment Required";
  public static final String Forbidden = "403 Forbidden";
  public static final String NotFound = "404 Not Found";
  public static final String MethodNotAllowed = "405 Method Not Allowed";
  public static final String NotAcceptable = "406 Not Acceptable";
  public static final String ProxyAuthenticationRequired = "407 Proxy Authentication Required";
  public static final String RequestTimeout = "408 Request Timeout";
  public static final String Conflict = "409 Conflict";
  public static final String Gone = "410 Gone";
  public static final String LengthRequired = "411 Length Required";
  public static final String PreconditionFailed = "412 Precondition Failed";
  public static final String PayloadTooLarge = "413 Payload Too Large";
  public static final String URITooLong = "414 URI Too Long";
  public static final String UnsupportedMediaType = "415 Unsupported Media Type";
  public static final String RangeNotSatisfiable = "416 Range Not Satisfiable";
  public static final String ExpectationFailed = "417 Expectation Failed";
  public static final String Imateapot = "418 I'm a teapot";
  public static final String MisdirectedRequest = "421 Misdirected Request";
  public static final String UnprocessableEntity = "422 Unprocessable Entity";
  public static final String Locked = "423 Locked";
  public static final String FailedDependency = "424 Failed Dependency";
  public static final String UpgradeRequired = "426 Upgrade Required";
  public static final String PreconditionRequired = "428 Precondition Required";
  public static final String TooManyRequests = "429 Too Many Requests";
  public static final String RequestHeaderFieldsTooLarge = "431 Request Header Fields Too Large";
  public static final String UnavailableForLegalReasons = "451 Unavailable For Legal Reasons";
  
  // 5xx Server errors
  public static final String InternalServerError = "500 Internal Server Error";
  public static final String NotImplemented = "501 Not Implemented";
  public static final String BadGateway = "502 Bad Gateway";
  public static final String ServiceUnavailable = "503 Service Unavailable";
  public static final String GatewayTimeout = "504 Gateway Timeout";
  public static final String HTTPVersionNotSupported = "505 HTTP Version Not Supported";
  public static final String VariantAlsoNegotiates = "506 Variant Also Negotiates";
  public static final String InsufficientStorage = "507 Insufficient Storage";
  public static final String LoopDetected = "508 Loop Detected";
  public static final String NotExtended = "510 Not Extended";
  public static final String NetworkAuthenticationRequired = "511 Network Authentication Required";

  public static Response of(final String statusCode) {
    return new Response(statusCode, Headers.empty(), "");
  }

  public static Response of(final String statusCode, final String entity) {
    return new Response(statusCode, Headers.empty(), entity);
  }

  public static Response of(final String statusCode, final Headers<ResponseHeader> headers) {
    return new Response(statusCode, headers, "");
  }

  public static Response of(final String statusCode, final Headers<ResponseHeader> headers, final String entity) {
    return new Response(statusCode, headers, entity);
  }

  public final String statusCode;
  public final Headers<ResponseHeader> headers;
  public final String entity;

  public Header headerOf(final String name) {
    for (final Header header : headers) {
      if (header.name.equals(name)) {
        return header;
      }
    }
    return null;
  }

  public ConsumerByteBuffer into(final ConsumerByteBuffer consumerByteBuffer) {
    return consumerByteBuffer.put(Converters.textToBytes(toString())).flip();
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder(size());
    
    // TODO: currently supports only HTTP/1.1
    
    builder.append(Version.HTTP_1_1).append(" ").append(statusCode).append("\n");
    appendAllHeadersTo(builder);
    builder.append("\n").append(entity);
    
    return builder.toString();
  }
  
  Response(final String statusCode, final Headers<ResponseHeader> headers, final String entity) {
    this.statusCode = statusCode;
    this.headers = headers;
    this.entity = entity;
  }

  private StringBuilder appendAllHeadersTo(final StringBuilder builder) {
    for (final ResponseHeader header : headers) {
      builder.append(header.name).append(": ").append(header.value).append("\n");
    }
    return builder;
  }

  private int size() {
    int headersSize = 0;
    for (final ResponseHeader header : headers) {
      // name + ": " + value + "\n"
      headersSize += (header.name.length() + 2 + header.value.length() + 1);
    }
    // HTTP/1.1 + 1 + status code + "\n" + headers + "\n" + entity + just-in-case
    return 9 + statusCode.length() + 1 + headersSize + 1 + entity.length() + 5;
  }
}
