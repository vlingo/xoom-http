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

import java.net.URI;
import java.nio.ByteBuffer;

import org.junit.Before;
import org.junit.Test;

import io.vlingo.wire.message.ByteBufferAllocator;
import io.vlingo.wire.message.Converters;

public class HttpRequestTest {
  private final ByteBuffer buffer = ByteBufferAllocator.allocate(1024);
  private String requestOneHeader;
  private String requestOneHeaderWithBody;
  private String requestMultiHeaders;
  private String requestMultiHeadersWithBody;
  
  @Test
  public void testThatRequestCanHaveOneHeader() throws Exception {
    final HttpRequest request = HttpRequest.from(toByteBuffer(requestOneHeader));
    
    assertNotNull(request);
    assertTrue(request.method.isGET());
    assertEquals(new URI("/"), request.uri);
    assertTrue(request.version.isHttp1_1());
    assertEquals(1, request.headers.size());
    assertFalse(request.body.hasLines());
  }
  
  @Test
  public void testThatRequestCanHaveOneHeaderWithBody() throws Exception {
    final HttpRequest request = HttpRequest.from(toByteBuffer(requestOneHeaderWithBody));
    
    assertNotNull(request);
    assertTrue(request.method.isGET());
    assertEquals(new URI("/one/two/three"), request.uri);
    assertTrue(request.version.isHttp1_1());
    assertEquals(1, request.headers.size());
    assertTrue(request.body.hasLines());
    assertNotNull(request.body.line(0));
    assertFalse(request.body.line(0).isEmpty());
  }
  
  @Test
  public void testThatRequestCanHaveMutipleHeaders() throws Exception {
    final HttpRequest request = HttpRequest.from(toByteBuffer(requestMultiHeaders));
    
    assertNotNull(request);
    assertTrue(request.method.isGET());
    assertEquals(new URI("/one"), request.uri);
    assertTrue(request.version.isHttp1_1());
    assertEquals(3, request.headers.size());
    assertFalse(request.body.hasLines());
  }
  
  @Test
  public void testThatRequestCanHaveMutipleHeadersAndBody() throws Exception {
    final HttpRequest request = HttpRequest.from(toByteBuffer(requestMultiHeadersWithBody));
    
    assertNotNull(request);
    assertTrue(request.method.isGET());
    assertEquals(new URI("/one/two/"), request.uri);
    assertTrue(request.version.isHttp1_1());
    assertEquals(3, request.headers.size());
    assertTrue(request.body.hasLines());
    assertNotNull(request.body.line(0));
    assertFalse(request.body.line(0).isEmpty());
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testRejectBogusMethodRequest() {
    HttpRequest.from(toByteBuffer("BOGUS / HTTP/1.1\nHost: test.com\n\n"));
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testRejectUnsupportedVersionRequest() {
    HttpRequest.from(toByteBuffer("GET / HTTP/1.0\nHost: test.com\n\n"));
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testRejectBadRequestNoHeader() {
    HttpRequest.from(toByteBuffer("GET / HTTP/1.1\n\n"));
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void testRejectBadRequestMissingLine() {
    HttpRequest.from(toByteBuffer("GET / HTTP/1.1\nHost: test.com\n"));
  }
  
  @Test
  public void testFindHeader() {
    final HttpRequest request = HttpRequest.from(toByteBuffer(requestOneHeaderWithBody));
    
    assertNotNull(request.headerOf(HttpRequestHeader.Host));
    assertEquals(HttpRequestHeader.Host, request.headerOf(HttpRequestHeader.Host).name);
    assertEquals("test.com", request.headerOf(HttpRequestHeader.Host).value);
  }
  
  @Test
  public void testFindHeaders() {
    final HttpRequest request = HttpRequest.from(toByteBuffer(requestMultiHeaders));
    
    assertNotNull(request.headerOf(HttpRequestHeader.Host));
    assertEquals(HttpRequestHeader.Host, request.headerOf(HttpRequestHeader.Host).name);
    assertEquals("test.com", request.headerOf(HttpRequestHeader.Host).value);
    
    assertNotNull(request.headerOf(HttpRequestHeader.Accept));
    assertEquals(HttpRequestHeader.Accept, request.headerOf(HttpRequestHeader.Accept).name);
    assertEquals("text/plain", request.headerOf(HttpRequestHeader.Accept).value);
    
    assertNotNull(request.headerOf(HttpRequestHeader.CacheControl));
    assertEquals(HttpRequestHeader.CacheControl, request.headerOf(HttpRequestHeader.CacheControl).name);
    assertEquals("no-cache", request.headerOf(HttpRequestHeader.CacheControl).value);
  }

  @Before
  public void setUp() {
    requestOneHeader = "GET / HTTP/1.1\nHost: test.com\n\n";
    
    requestOneHeaderWithBody = "GET /one/two/three HTTP/1.1\nHost: test.com\n\n{ text:\"some text\"}";
    
    requestMultiHeaders = "GET /one HTTP/1.1\nHost: test.com\nAccept: text/plain\nCache-Control: no-cache\n\n";
    
    requestMultiHeadersWithBody = "GET /one/two/ HTTP/1.1\nHost: test.com\nAccept: text/plain\nCache-Control: no-cache\n\n{ text:\"some text\"}";
  }
  
  private ByteBuffer toByteBuffer(final String requestContent) {
    buffer.clear();
    buffer.put(Converters.textToBytes(requestContent));
    buffer.flip();
    return buffer;
  }
}
