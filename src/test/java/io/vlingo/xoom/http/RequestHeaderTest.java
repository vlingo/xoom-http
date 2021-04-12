// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RequestHeaderTest {

  @Test
  public void testHeaderNameValue() {
    final RequestHeader header = RequestHeader.of(RequestHeader.Accept, "text/plain");
    
    assertEquals(RequestHeader.Accept, header.name);
    assertEquals("text/plain", header.value);
  }
  
  @Test
  public void testParseHeader() {
    final RequestHeader header = RequestHeader.from("Accept: text/plain");
    
    assertEquals(RequestHeader.Accept, header.name);
    assertEquals("text/plain", header.value);
  }
  
  @Test
  public void testParseSpaceyHeader() {
    final RequestHeader header = RequestHeader.from("  Accept:    text/plain  ");
    
    assertEquals(RequestHeader.Accept, header.name);
    assertEquals("text/plain", header.value);
  }

  @Test
  public void testParseLowerCaseContentLength() {
    final RequestHeader header = RequestHeader.from("content-length: 10");

    assertEquals(10, header.ifContentLength());
  }

  @Test
  public void testEqualsCaseInsensitive() {
    final RequestHeader header1 = RequestHeader.from("Content-length: 10");
    final RequestHeader header2 = RequestHeader.from("content-length: 10");

    assertEquals(header1, header2);
  }

  @Test
  public void testParseHeaderWithMultipleValueStrings() {
    final RequestHeader header = RequestHeader.from("Cookie: $Version=1; Skin=new;");
    
    assertEquals(RequestHeader.Cookie, header.name);
    assertEquals("$Version=1; Skin=new;", header.value);
  }
  
  @Test
  public void testParseHeaderWithMultipleValueStringsAndColons() {
    final RequestHeader header = RequestHeader.from("Accept-Datetime: Thu, 31 May 2007 20:35:00 GMT");
    
    assertEquals(RequestHeader.AcceptDatetime, header.name);
    assertEquals("Thu, 31 May 2007 20:35:00 GMT", header.value);
  }

  @Test
  public void testContentEncodingHeaderFromString() {
    final RequestHeader header = RequestHeader.from("Content-Encoding: deflate, gzip");

    assertEquals(RequestHeader.ContentEncoding, header.name);
    assertEquals("deflate, gzip", header.value);
  }

  @Test
  public void testContentEncodingJoinsMethods() {
    final RequestHeader header = RequestHeader.contentEncoding("foo", "bar");

    assertEquals(RequestHeader.ContentEncoding, header.name);
    assertEquals("foo,bar", header.value);
  }

  @Test
  public void testContentEncodingEmpty() {
    final RequestHeader header = RequestHeader.contentEncoding();

    assertEquals(RequestHeader.ContentEncoding, header.name);
    assertEquals("", header.value);
  }
}
