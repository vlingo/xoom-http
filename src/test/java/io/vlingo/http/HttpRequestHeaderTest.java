// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HttpRequestHeaderTest {

  @Test
  public void testHeaderNameValue() {
    final HttpRequestHeader header = HttpRequestHeader.of(HttpRequestHeader.Accept, "text/plain");
    
    assertEquals(HttpRequestHeader.Accept, header.name);
    assertEquals("text/plain", header.value);
  }
  
  @Test
  public void testParseHeader() {
    final HttpRequestHeader header = HttpRequestHeader.from("Accept: text/plain");
    
    assertEquals(HttpRequestHeader.Accept, header.name);
    assertEquals("text/plain", header.value);
  }
  
  @Test
  public void testParseSpaceyHeader() {
    final HttpRequestHeader header = HttpRequestHeader.from("  Accept:    text/plain  ");
    
    assertEquals(HttpRequestHeader.Accept, header.name);
    assertEquals("text/plain", header.value);
  }
  
  @Test
  public void testParseHeaderWithMultipleValueStrings() {
    final HttpRequestHeader header = HttpRequestHeader.from("Cookie: $Version=1; Skin=new;");
    
    assertEquals(HttpRequestHeader.Cookie, header.name);
    assertEquals("$Version=1; Skin=new;", header.value);
  }
  
  @Test
  public void testParseHeaderWithMultipleValueStringsAndColons() {
    final HttpRequestHeader header = HttpRequestHeader.from("Accept-Datetime: Thu, 31 May 2007 20:35:00 GMT");
    
    assertEquals(HttpRequestHeader.AcceptDatetime, header.name);
    assertEquals("Thu, 31 May 2007 20:35:00 GMT", header.value);
  }
}
