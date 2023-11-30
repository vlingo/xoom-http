// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http;

import io.vlingo.xoom.wire.message.Converters;

/**
 * The standard HTTP request header along with standard type names and
 * convenience factory methods for frequently used headers.
 * <p>
 * @see <a href="https://en.wikipedia.org/wiki/List_of_HTTP_header_fields">Wikipedia.org:header_fields</a>
 * <p>
 * @see <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html">www.w3.org:protocol-rfc2616</a>
 */
public class RequestHeader extends Header {
  public static final String Accept = "Accept";
  public static final String AcceptCharset = "Accept-Charset";
  public static final String AcceptEncoding = "Accept-Encoding";
  public static final String AcceptLanguage = "Accept-Language";
  public static final String AcceptDatetime = "Accept-Datetime";
  public static final String AccessControlRequestMethod = "Access-Control-Request-Method";
  public static final String AccessControlRequestHeaders = "Access-Control-Request-Headers";
  public static final String Authorization = "Authorization";
  public static final String CacheControl = "Cache-Control";
  public static final String Connection = "Connection";
  public static final String Cookie = "Cookie";
  public static final String ContentLength = "Content-Length";
  public static final String ContentEncoding = "Content-Encoding";
  public static final String ContentMD5 = "Content-MD5";
  public static final String ContentType = "Content-Type";
  public static final String Date = "Date";
  public static final String Expect = "Expect";
  public static final String Forwarded = "Forwarded";
  public static final String From = "From";
  public static final String Host = "Host";
  public static final String IfMatch = "If-Match";
  public static final String IfModifiedSince = "If-Modified-Since";
  public static final String IfNoneMatch = "If-None-Match";
  public static final String IfRange = "If-Range";
  public static final String IfUnmodifiedSince = "If-Unmodified-Since";
  public static final String LastEventID = "Last-Event-ID";
  public static final String MaxForwards = "Max-Forwards";
  public static final String Origin = "Origin";
  public static final String Pragma = "Pragma";
  public static final String ProxyAuthorization = "Proxy-Authorization";
  public static final String Range = "Range";
  public static final String Referer = "Referer";
  public static final String TE = "TE";
  public static final String UserAgent = "User-Agent";
  public static final String Upgrade = "Upgrade";
  public static final String Via = "Via";
  public static final String Warning = "Warning";

  // Common non-standard request header names
  public static final String XRequestedWith = "X-Requested-With";
  public static final String DNT = "DNT";
  public static final String XForwardedHost = "X-Forwarded-Host";
  public static final String XForwardedProto = "X-Forwarded-Proto";
  public static final String FrontEndHttps = "Front-End-Https";
  public static final String XHttpMethodOverride = "X-Http-Method-Override";
  public static final String XATTDeviceId = "X-ATT-DeviceId";
  public static final String XWapProfile = "X-Wap-Profile";
  public static final String ProxyConnection = "Proxy-Connection";
  public static final String XUIDH = "X-UIDH";
  public static final String XCsrfToken = "X-Csrf-Token";
  public static final String XRequestID = "X-Request-ID";
  public static final String XCorrelationID = "X-Correlation-ID";

  public static RequestHeader from(final String textLine) {
    final int colonIndex = textLine.indexOf(":");

    if (colonIndex == -1) {
      throw new IllegalArgumentException("Not a header: " + textLine);
    }

    return new RequestHeader(textLine.substring(0, colonIndex).trim(), textLine.substring(colonIndex+1).trim());
  }

  public static RequestHeader accept(final String type) {
    return new RequestHeader(Accept, type);
  }

  public static RequestHeader cacheControl(final String option) {
    return new RequestHeader(CacheControl, option);
  }

  public static RequestHeader connection(final String value) {
    return new RequestHeader(Connection, value);
  }

  public static RequestHeader contentLength(final int length) {
    return new RequestHeader(ContentLength, String.valueOf(length));
  }

  public static RequestHeader contentLength(final String body) {
    return new RequestHeader(ContentLength, String.valueOf(Converters.encodedLength(body)));
  }

  public static RequestHeader contentLength(final byte[] body) {
    return new RequestHeader(ContentLength, String.valueOf(body.length));
  }

  public static RequestHeader contentType(final String type) {
    return new RequestHeader(ContentType, type);
  }

  public static RequestHeader correlationId(final String correlationId) {
    return new RequestHeader(XCorrelationID, correlationId);
  }

  public static RequestHeader contentEncoding(final String ... encodingMethod) {
    return (encodingMethod.length > 0) ?
       new RequestHeader(ContentEncoding, String.join(",", encodingMethod)) :
       new RequestHeader(ContentEncoding, "");
  }

  public static RequestHeader host(final String value) {
    return new RequestHeader(Host, value);
  }

  public static RequestHeader keepAlive() {
    return new RequestHeader(Connection, Header.ValueKeepAlive);
  }

  public static RequestHeader of(final String name, final String value) {
    return new RequestHeader(name, value);
  }

  /**
   * Answer the {@code int} value of the {@code ContentLength} header, or {@code 0} if missing.
   * @return int
   */
  public int ifContentLength() {
    if (name.equalsIgnoreCase(ContentLength)) {
      return Integer.parseInt(value);
    }
    return 0;
  }

  /**
   * Construct my state.
   * @param name the String to set as my name
   * @param value the String to set as my value
   */
  private RequestHeader(final String name, final String value) {
    super(name, value);
  }
}
