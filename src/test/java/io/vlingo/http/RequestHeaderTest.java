// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

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
}
