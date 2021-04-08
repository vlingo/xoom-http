// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http;

import static io.vlingo.xoom.http.Response.Status.Continue;
import static io.vlingo.xoom.http.Response.Status.NoContent;
import static io.vlingo.xoom.http.Response.Status.NotModified;
import static io.vlingo.xoom.http.Response.Status.Ok;
import static io.vlingo.xoom.http.ResponseHeader.CacheControl;
import static io.vlingo.xoom.http.ResponseHeader.ContentLength;
import static io.vlingo.xoom.http.ResponseHeader.ContentType;
import static io.vlingo.xoom.http.ResponseHeader.ETag;
import static io.vlingo.xoom.http.ResponseHeader.headers;
import static io.vlingo.xoom.http.ResponseHeader.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.vlingo.xoom.http.Header.Headers;
import io.vlingo.xoom.wire.message.Converters;

public class ResponseTest {

  @Test
  public void testResponseWithOneHeaderNoEntity() {
    final Response response = Response.of(Version.Http1_1, Ok, headers(CacheControl, "max-age=3600"));

    final String facsimile = "HTTP/1.1 200 OK\nCache-Control: max-age=3600\nContent-Length: 0\n\n";

    assertEquals(facsimile, response.toString());
  }

  @Test
  public void testResponseWithOneHeaderAndEntity() {
    final String body = "{ text : \"some text\" }";
    final Response response = Response.of(Version.Http1_1, Ok, headers(CacheControl, "max-age=3600"), body);

    final String facsimile = "HTTP/1.1 200 OK\nCache-Control: max-age=3600\nContent-Length: " + body.length() + "\n\n{ text : \"some text\" }";

    assertEquals(facsimile, response.toString());
  }

  @Test
  public void testBinaryBodyResponseWithOneHeaderAndEntity() {
    final byte[] body = { 1, 2, 1, 2 };
    final Response response = Response.of(Version.Http1_1, Ok,
      Header.Headers.of(
        ResponseHeader.of(ContentType, "application/octet-stream"),
        ResponseHeader.of(ContentLength, Integer.toString(body.length))
      ), Body.from(body, Body.Encoding.None));


    assertEquals(body, response.entity.binaryContent());
  }

  @Test
  public void testResponseWithMultipleHeadersNoEntity() {
    final Response response = Response.of(Version.Http1_1, Ok, headers(of(ETag, "123ABC")).and(of(CacheControl, "max-age=3600")));

    final String facsimile = "HTTP/1.1 200 OK\nETag: 123ABC\nCache-Control: max-age=3600\nContent-Length: 0\n\n";

    assertEquals(facsimile, response.toString());
  }

  @Test
  public void testResponseWithMultipleHeadersAndEntity() {
    final String body = "{ text : \"some text\" }";
    final Response response = Response.of(Version.Http1_1, Ok, headers(of(ETag, "123ABC")).and(of(CacheControl, "max-age=3600")), body);

    final String facsimile = "HTTP/1.1 200 OK\nETag: 123ABC\nCache-Control: max-age=3600\nContent-Length: " + body.length() + "\n\n{ text : \"some text\" }";

    assertEquals(facsimile, response.toString());
  }

  @Test
  public void testThatChunkedResponseIsValid() {
    final String Chunk1 = "ABCDEFGHIJKLMNOPQRSTUVWYYZ0123";
    final String Chunk2 = "abcdefghijklmnopqrstuvwxyz012345";

    final String chunks = Integer.toHexString(Chunk1.length()) + "\r\n" + Chunk1 + "\r\n" + Integer.toHexString(Chunk2.length()) + "\r\n" + Chunk2 + "\r\n0\r\n";
    final String responseMultiHeadersWithChunkedBody =
            "HTTP/1.1 200 OK\nTransfer-Encoding: chunked\nCache-Control: no-cache\n\n" + chunks;

    final Response response =
            Response.of(
                    Version.Http1_1,
                    Ok,
                    headers(of(ResponseHeader.TransferEncoding, "chunked")).and(of(CacheControl, "no-cache")),
                    Body.beginChunked().appendChunk(Chunk1).appendChunk(Chunk2).end());

    assertEquals(responseMultiHeadersWithChunkedBody, response.toString());
  }

  @Test
  public void testItShouldSendNoContentLengthWithInformationalStatusCodes() {
    final Response response = Response.of(Version.Http1_1, Continue);

    final String facsimile = "HTTP/1.1 100 Continue\n\n";

    assertEquals(facsimile, response.toString());
  }

  @Test
  public void testItShouldSendNoContentLengthWithNoContentStatusCode() {
    final Response response = Response.of(Version.Http1_1, NoContent);

    final String facsimile = "HTTP/1.1 204 No Content\n\n";

    assertEquals(facsimile, response.toString());
  }

  @Test
  public void testItShouldSendNoContentLengthWithNotModifiedStatusCode() {
    final Response response = Response.of(Version.Http1_1, NotModified);

    final String facsimile = "HTTP/1.1 304 Not Modified\n\n";

    assertEquals(facsimile, response.toString());
  }

  @Test
  public void testExtendedCharactersContentLength() {
    final String asciiWithExtendedCharacters = ExtendedCharactersFixture.asciiWithExtendedCharacters();

    final Response response = Response.of(Response.Status.Ok, Headers.empty(), asciiWithExtendedCharacters);

    final int contentLength = Integer.parseInt(response.headerValueOr(RequestHeader.ContentLength, "0"));

    assertFalse(contentLength == 0);

    assertTrue(asciiWithExtendedCharacters.length() < contentLength);

    assertEquals(Converters.textToBytes(asciiWithExtendedCharacters).length, contentLength);
  }
}

