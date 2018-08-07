// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static io.vlingo.http.Method.*;

import java.net.URI;
import java.nio.ByteBuffer;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.wire.message.ByteBufferAllocator;
import io.vlingo.wire.message.Converters;

public class RequestTest {
  private final ByteBuffer buffer = ByteBufferAllocator.allocate(1024);
  private String requestOneHeader;
  private String requestTwoHeadersWithBody;
  private String requestMultiHeaders;
  private String requestMultiHeadersWithBody;
  private String requestQueryParameters;
  
  @Test
  public void testThatRequestCanHaveOneHeader() throws Exception {
    final Request request = Request.from(toByteBuffer(requestOneHeader));
    
    assertNotNull(request);
    assertTrue(request.method.isGET());
    assertEquals(new URI("/"), request.uri);
    assertTrue(request.version.isHttp1_1());
    assertEquals(1, request.headers.size());
    assertFalse(request.body.hasContent());
  }
  
  @Test
  public void testThatRequestCanHaveOneHeaderWithBody() throws Exception {
    final Request request = Request.from(toByteBuffer(requestTwoHeadersWithBody));
    
    assertNotNull(request);
    assertTrue(request.method.isPUT());
    assertEquals(new URI("/one/two/three"), request.uri);
    assertTrue(request.version.isHttp1_1());
    assertEquals(2, request.headers.size());
    assertTrue(request.body.hasContent());
    assertNotNull(request.body.content);
    assertFalse(request.body.content.isEmpty());
  }
  
  @Test
  public void testThatRequestCanHaveMutipleHeaders() throws Exception {
    final Request request = Request.from(toByteBuffer(requestMultiHeaders));
    
    assertNotNull(request);
    assertTrue(request.method.isGET());
    assertEquals(new URI("/one"), request.uri);
    assertTrue(request.version.isHttp1_1());
    assertEquals(3, request.headers.size());
    assertFalse(request.body.hasContent());
  }
  
  @Test
  public void testThatRequestCanHaveMutipleHeadersAndBody() throws Exception {
    final Request request = Request.from(toByteBuffer(requestMultiHeadersWithBody));
    
    assertNotNull(request);
    assertTrue(request.method.isPOST());
    assertEquals(new URI("/one/two/"), request.uri);
    assertTrue(request.version.isHttp1_1());
    assertEquals(4, request.headers.size());
    assertEquals(RequestHeader.Host, request.headerOf(RequestHeader.Host).name);
    assertEquals(RequestHeader.ContentLength, request.headerOf(RequestHeader.ContentLength).name);
    assertEquals(RequestHeader.Accept, request.headerOf(RequestHeader.Accept).name);
    assertEquals(RequestHeader.CacheControl, request.headerOf(RequestHeader.CacheControl).name);
    assertTrue(request.body.hasContent());
    assertNotNull(request.body.content);
    assertFalse(request.body.content.isEmpty());
    assertEquals(request.body.toString(), "{ text:\"some text\"}");
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testRejectBogusMethodRequest() {
    Request.from(toByteBuffer("BOGUS / HTTP/1.1\nHost: test.com\n\n"));
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testRejectUnsupportedVersionRequest() {
    Request.from(toByteBuffer("GET / HTTP/1.0\nHost: test.com\n\n"));
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testRejectBadRequestNoHeader() {
    Request.from(toByteBuffer("GET / HTTP/1.1\n\n"));
  }
  
  @Test(expected=IllegalStateException.class)
  public void testRejectBadRequestMissingLine() {
    Request.from(toByteBuffer("GET / HTTP/1.1\nHost: test.com\n"));
  }
  
  @Test
  public void testFindHeader() {
    final Request request = Request.from(toByteBuffer(requestTwoHeadersWithBody));
    
    assertNotNull(request.headerOf(RequestHeader.Host));
    assertEquals(RequestHeader.Host, request.headerOf(RequestHeader.Host).name);
    assertEquals(RequestHeader.ContentLength, request.headerOf(RequestHeader.ContentLength).name);
    assertEquals("test.com", request.headerOf(RequestHeader.Host).value);
  }
  
  @Test
  public void testFindHeaders() {
    final Request request = Request.from(toByteBuffer(requestMultiHeaders));
    
    assertNotNull(request.headerOf(RequestHeader.Host));
    assertEquals(RequestHeader.Host, request.headerOf(RequestHeader.Host).name);
    assertEquals("test.com", request.headerOf(RequestHeader.Host).value);
    
    assertNotNull(request.headerOf(RequestHeader.Accept));
    assertEquals(RequestHeader.Accept, request.headerOf(RequestHeader.Accept).name);
    assertEquals("text/plain", request.headerOf(RequestHeader.Accept).value);
    
    assertNotNull(request.headerOf(RequestHeader.CacheControl));
    assertEquals(RequestHeader.CacheControl, request.headerOf(RequestHeader.CacheControl).name);
    assertEquals("no-cache", request.headerOf(RequestHeader.CacheControl).value);
  }

  @Test
  public void testQueryParameters() {
    final Request request = Request.from(toByteBuffer(requestQueryParameters));
    final QueryParameters queryParameters = request.queryParameters();
    assertEquals(4, queryParameters.names().size());
    assertEquals("1", queryParameters.valuesOf("one").get(0));
    assertEquals("2", queryParameters.valuesOf("two").get(0));
    assertEquals("3", queryParameters.valuesOf("three").get(0));
    assertEquals("NY", queryParameters.valuesOf("state").get(0));
    assertEquals("CO", queryParameters.valuesOf("state").get(1));
  }

  @Test
  public void testRequestBuilder() {
    assertEquals(requestOneHeader,
            Request
              .has(Method.GET)
              .and(URI.create("/"))
              .and(RequestHeader.host("test.com"))
              .toString());

    assertEquals(requestOneHeader,
            Request
              .method(GET)
              .uri("/")
              .header(RequestHeader.Host, "test.com")
              .toString());

    assertEquals(requestTwoHeadersWithBody,
            Request
              .has(Method.PUT)
              .and(URI.create("/one/two/three"))
              .and(RequestHeader.host("test.com"))
              .and(RequestHeader.contentLength(19))
              .and(Body.from("{ text:\"some text\"}"))
              .toString());

    assertEquals(requestTwoHeadersWithBody,
            Request
              .method(PUT)
              .uri("/one/two/three")
              .header(RequestHeader.Host, "test.com")
              .header(RequestHeader.ContentLength, 19)
              .body("{ text:\"some text\"}")
              .toString());

    assertEquals(requestMultiHeaders,
            Request
              .has(Method.GET)
              .and(URI.create("/one"))
              .and(RequestHeader.host("test.com"))
              .and(RequestHeader.accept("text/plain"))
              .and(RequestHeader.cacheControl("no-cache"))
              .toString());

    assertEquals(requestMultiHeaders,
            Request
              .method(GET)
              .uri("/one")
              .header(RequestHeader.Host, "test.com")
              .header(RequestHeader.Accept, "text/plain")
              .header(RequestHeader.CacheControl, "no-cache")
              .toString());

    assertEquals(requestMultiHeadersWithBody,
            Request
              .has(Method.POST)
              .and(URI.create("/one/two/"))
              .and(RequestHeader.host("test.com"))
              .and(RequestHeader.contentLength(19))
              .and(RequestHeader.accept("text/plain"))
              .and(RequestHeader.cacheControl("no-cache"))
              .and(Body.from("{ text:\"some text\"}"))
              .toString());

    assertEquals(requestMultiHeadersWithBody,
            Request
              .has(POST)
              .uri("/one/two/")
              .header(RequestHeader.Host, "test.com")
              .header(RequestHeader.ContentLength, 19)
              .header(RequestHeader.Accept, "text/plain")
              .header(RequestHeader.CacheControl, "no-cache")
              .body("{ text:\"some text\"}")
              .toString());
  }

  @Before
  public void setUp() {
    requestOneHeader = "GET / HTTP/1.1\nHost: test.com\n\n";
    
    requestTwoHeadersWithBody = "PUT /one/two/three HTTP/1.1\nHost: test.com\nContent-Length: 19\n\n{ text:\"some text\"}";
    
    requestMultiHeaders = "GET /one HTTP/1.1\nHost: test.com\nAccept: text/plain\nCache-Control: no-cache\n\n";
    
    requestMultiHeadersWithBody = "POST /one/two/ HTTP/1.1\nHost: test.com\nContent-Length: 19\nAccept: text/plain\nCache-Control: no-cache\n\n{ text:\"some text\"}";
    
    requestQueryParameters = "GET /one/param1?one=1&two=2&three=3&state=NY&state=CO HTTP/1.1\nHost: test.com\n\n";
  }
  
  private ByteBuffer toByteBuffer(final String requestContent) {
    buffer.clear();
    buffer.put(Converters.textToBytes(requestContent));
    buffer.flip();
    return buffer;
  }
}
