// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class HttpResponseTest {

  @Test
  public void testResponseWithOneHeaderNoEntity() {
    final HttpResponse response = HttpResponse.from(HttpResponse.Ok, oneHeader(), "");
    
    final String facsimile = "HTTP/1.1 200 OK\nCache-Control: max-age=3600\n\n";
    
    assertEquals(facsimile, response.toString());
  }

  @Test
  public void testResponseWithOneHeaderAndEntity() {
    final HttpResponse response = HttpResponse.from(HttpResponse.Ok, oneHeader(), "{ text : \"some text\" }");
    
    final String facsimile = "HTTP/1.1 200 OK\nCache-Control: max-age=3600\n\n{ text : \"some text\" }";
    
    assertEquals(facsimile, response.toString());
  }

  @Test
  public void testResponseWithMultipleHeadersNoEntity() {
    final HttpResponse response = HttpResponse.from(HttpResponse.Ok, mutipleHeaders(), "");
    
    final String facsimile = "HTTP/1.1 200 OK\nETag: 123ABC\nCache-Control: max-age=3600\n\n";
    
    assertEquals(facsimile, response.toString());
  }

  @Test
  public void testResponseWithMultipleHeadersAndEntity() {
    final HttpResponse response = HttpResponse.from(HttpResponse.Ok, mutipleHeaders(), "{ text : \"some text\" }");
    
    final String facsimile = "HTTP/1.1 200 OK\nETag: 123ABC\nCache-Control: max-age=3600\n\n{ text : \"some text\" }";
    
    assertEquals(facsimile, response.toString());
  }

  private List<HttpResponseHeader> oneHeader() {
    return Arrays.asList(HttpResponseHeader.of(HttpResponseHeader.CacheControl, "max-age=3600"));
  }

  private List<HttpResponseHeader> mutipleHeaders() {
    return Arrays.asList(
            HttpResponseHeader.of(HttpResponseHeader.ETag, "123ABC"),
            HttpResponseHeader.of(HttpResponseHeader.CacheControl, "max-age=3600"));
  }
}
