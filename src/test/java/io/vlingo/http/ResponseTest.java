// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import static io.vlingo.http.Response.Status.Ok;
import static io.vlingo.http.ResponseHeader.CacheControl;
import static io.vlingo.http.ResponseHeader.ETag;
import static io.vlingo.http.ResponseHeader.headers;
import static io.vlingo.http.ResponseHeader.of;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ResponseTest {

  @Test
  public void testResponseWithOneHeaderNoEntity() {
    final Response response = Response.of(Version.Http1_1, Ok, headers(CacheControl, "max-age=3600"));

    final String facsimile = "HTTP/1.1 200 OK\nCache-Control: max-age=3600\n\n";

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
  public void testResponseWithMultipleHeadersNoEntity() {
    final Response response = Response.of(Version.Http1_1, Ok, headers(of(ETag, "123ABC")).and(of(CacheControl, "max-age=3600")));

    final String facsimile = "HTTP/1.1 200 OK\nETag: 123ABC\nCache-Control: max-age=3600\n\n";

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
}

