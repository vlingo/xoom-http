// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http;

/**
 * The standard HTTP response header along with standard type names and
 * convenience factory methods for frequently used headers.
 * <p>
 * @see <a href="https://en.wikipedia.org/wiki/List_of_HTTP_header_fields">Wikipedia.org:header_fields</a>
 * <p>
 * @see <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html">www.w3.org:protocol-rfc2616</a>
 */
public class ResponseHeader extends Header {
  public static final String AccessControlAllowOrigin = "Access-Control-Allow-Origin";
  public static final String AccessControlAllowCredentials = "Access-Control-Allow-Credentials";
  public static final String AccessControlExposeHeaders = "Access-Control-Expose-Headers";
  public static final String AccessControlMaxAge = "Access-Control-Max-Age";
  public static final String AccessControlAllowMethods = "Access-Control-Allow-Methods";
  public static final String AccessControlAllowHeaders = "Access-Control-Allow-Headers";
  public static final String AcceptPatch = "Accept-Patch";
  public static final String AcceptRanges = "Accept-Ranges";
  public static final String Age = "Age";
  public static final String Allow = "Allow";
  public static final String AltSvc = "Alt-Svc";
  public static final String CacheControl = "Cache-Control";
  public static final String Connection = "Connection";
  public static final String ContentDisposition = "Content-Disposition";
  public static final String ContentEncoding = "Content-Encoding";
  public static final String ContentLanguage = "Content-Language";
  public static final String ContentLength = "Content-Length";
  public static final String ContentLocation = "Content-Location";
  public static final String ContentMD5 = "Content-MD5";
  public static final String ContentRange = "Content-Range";
  public static final String ContentType = "Content-Type";
  public static final String Date = "Date";
  public static final String ETag = "ETag";
  public static final String Expires = "Expires";
  public static final String LastModified = "Last-Modified";
  public static final String Link = "Link";
  public static final String Location = "Location";
  public static final String P3P = "P3P";
  public static final String Pragma = "Pragma";
  public static final String ProxyAuthenticate = "Proxy-Authenticate";
  public static final String PublicKeyPins = "Public-Key-Pins";
  public static final String RetryAfter = "Retry-After";
  public static final String Server = "Server";
  public static final String SetCookie = "Set-Cookie";
  public static final String StrictTransportSecurity = "Strict-Transport-Security";
  public static final String Trailer = "Trailer";
  public static final String TransferEncoding = "Transfer-Encoding";
  public static final String Tk = "Tk";
  public static final String Upgrade = "Upgrade";
  public static final String Vary = "Vary";
  public static final String Via = "Via";
  public static final String Warning = "Warning";
  public static final String WWWAuthenticate = "WWW-Authenticate";
  public static final String XFrameOptions = "X-Frame-Options";

  // Common non-standard response header names
  public static final String ContentSecurityPolicy = "Content-Security-Policy";
  public static final String XContentSecurityPolicy = "X-Content-Security-Policy";
  public static final String XWebKitCSP = "X-WebKit-CSP";
  public static final String Refresh = "Refresh";
  public static final String Status = "Status";
  public static final String TimingAllowOrigin = "Timing-Allow-Origin";
  public static final String UpgradeInsecureRequests = "Upgrade-Insecure-Requests";
  public static final String XContentDuration = "X-Content-Duration";
  public static final String XContentTypeOptions = "X-Content-Type-Options";
  public static final String XPoweredBy = "X-Powered-By";
  public static final String XRequestID = "X-Request-ID";
  public static final String XCorrelationID = "X-Correlation-ID";
  public static final String XUACompatible = "X-UA-Compatible";
  public static final String XXSSProtection = "X-XSS-Protection";

  public static ResponseHeader from(final String textLine) {
    final int colonIndex = textLine.indexOf(":");

    if (colonIndex == -1) {
      throw new IllegalArgumentException("Not a header: " + textLine);
    }

    return new ResponseHeader(textLine.substring(0, colonIndex).trim(), textLine.substring(colonIndex+1).trim());
  }

  public static Headers<ResponseHeader> headers(final String name, final String value) {
    return Headers.of(of(name, value));
  }

  public static Headers<ResponseHeader> headers(final ResponseHeader header) {
    return Headers.of(header);
  }

  public static ResponseHeader contentLength(final int length) {
    return new ResponseHeader(ContentLength, String.valueOf(length));
  }

  public static ResponseHeader contentLength(final String body) {
    return new ResponseHeader(ContentLength, String.valueOf(body.length()));
  }

  public static ResponseHeader contentLength(final byte[] body) {
    return new ResponseHeader(ContentLength, String.valueOf(body.length));
  }

  public static ResponseHeader contentType(final String type) {
    return new ResponseHeader(ContentType, type);
  }

  public static ResponseHeader correlationId(final String correlationId) {
    return new ResponseHeader(XCorrelationID, correlationId);
  }

  public static ResponseHeader of(final String name, final String value) {
    return new ResponseHeader(name, value);
  }

  public static ResponseHeader of(final String name, final int value) {
    return new ResponseHeader(name, String.valueOf(value));
  }

  public static ResponseHeader of(final String name, final long value) {
    return new ResponseHeader(name, String.valueOf(value));
  }

  /**
   * Answer the {@code int} value of the {@code ContentLength} header, or {@code 0} if missing.
   * @return int
   */
  public int ifContentLength() {
    if (name.equalsIgnoreCase(ContentLength)) {
      return Integer.parseInt(value);
    }
    return -1;
  }

  /**
   * Answer whether or not I have a {@code Connection} with {@code keep-alive} header.
   * @return boolean
   */
  public boolean isKeepAliveConnection() {
    return name.equalsIgnoreCase(Connection) && value.equalsIgnoreCase("keep-alive");
  }

  /**
   * Answer whether or not I have a {@code ContentType} with a kind of {@code -stream} header.
   * @return boolean
   */
  public boolean isStreamContentType() {
    return name.equalsIgnoreCase(ContentType) && value.toLowerCase().contains("-stream");
  }

  /**
   * Answer whether or not I have a {@code TransferEncoding} of {@code chunked} header.
   * @return boolean
   */
  public boolean isTransferEncodingChunked() {
    return name.equalsIgnoreCase(TransferEncoding) && value.equalsIgnoreCase("chunked");
  }

  /**
   * Construct my state.
   * @param name the String to set as my name
   * @param value the String to set as my value
   */
  private ResponseHeader(final String name, final String value) {
    super(name, value);
  }
}
