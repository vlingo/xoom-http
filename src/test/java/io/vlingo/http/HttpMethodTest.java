// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HttpMethodTest {

  @Test
  public void testPOST() {
    final HttpMethod method = HttpMethod.from("POST");
    assertTrue(method.isPOST());
    
    assertFalse(method.isCONNECT());
    assertFalse(method.isDELETE());
    assertFalse(method.isGET());
    assertFalse(method.isHEAD());
    assertFalse(method.isOPTIONS());
    assertFalse(method.isPATCH());
    assertFalse(method.isPUT());
    assertFalse(method.isTRACE());
  }

  @Test
  public void testGET() {
    final HttpMethod method = HttpMethod.from("GET");
    assertTrue(method.isGET());
    
    assertFalse(method.isCONNECT());
    assertFalse(method.isDELETE());
    assertFalse(method.isHEAD());
    assertFalse(method.isOPTIONS());
    assertFalse(method.isPATCH());
    assertFalse(method.isPOST());
    assertFalse(method.isPUT());
    assertFalse(method.isTRACE());
  }

  @Test
  public void testPUT() {
    final HttpMethod method = HttpMethod.from("PUT");
    assertTrue(method.isPUT());
    
    assertFalse(method.isCONNECT());
    assertFalse(method.isDELETE());
    assertFalse(method.isGET());
    assertFalse(method.isHEAD());
    assertFalse(method.isOPTIONS());
    assertFalse(method.isPATCH());
    assertFalse(method.isPOST());
    assertFalse(method.isTRACE());
  }

  @Test
  public void testPATCH() {
    final HttpMethod method = HttpMethod.from("PATCH");
    assertTrue(method.isPATCH());
    
    assertFalse(method.isCONNECT());
    assertFalse(method.isDELETE());
    assertFalse(method.isGET());
    assertFalse(method.isHEAD());
    assertFalse(method.isOPTIONS());
    assertFalse(method.isPUT());
    assertFalse(method.isPOST());
    assertFalse(method.isTRACE());
  }

  @Test
  public void testDELETE() {
    final HttpMethod method = HttpMethod.from("DELETE");
    assertTrue(method.isDELETE());
    
    assertFalse(method.isCONNECT());
    assertFalse(method.isGET());
    assertFalse(method.isHEAD());
    assertFalse(method.isOPTIONS());
    assertFalse(method.isPATCH());
    assertFalse(method.isPUT());
    assertFalse(method.isPOST());
    assertFalse(method.isTRACE());
  }

  @Test
  public void testHEAD() {
    final HttpMethod method = HttpMethod.from("HEAD");
    assertTrue(method.isHEAD());
    
    assertFalse(method.isCONNECT());
    assertFalse(method.isDELETE());
    assertFalse(method.isGET());
    assertFalse(method.isOPTIONS());
    assertFalse(method.isPATCH());
    assertFalse(method.isPUT());
    assertFalse(method.isPOST());
    assertFalse(method.isTRACE());
  }

  @Test
  public void testTRACE() {
    final HttpMethod method = HttpMethod.from("TRACE");
    assertTrue(method.isTRACE());
    
    assertFalse(method.isCONNECT());
    assertFalse(method.isDELETE());
    assertFalse(method.isGET());
    assertFalse(method.isHEAD());
    assertFalse(method.isOPTIONS());
    assertFalse(method.isPATCH());
    assertFalse(method.isPUT());
    assertFalse(method.isPOST());
  }

  @Test
  public void testOPTIONS() {
    final HttpMethod method = HttpMethod.from("OPTIONS");
    assertTrue(method.isOPTIONS());
    
    assertFalse(method.isCONNECT());
    assertFalse(method.isDELETE());
    assertFalse(method.isGET());
    assertFalse(method.isHEAD());
    assertFalse(method.isPATCH());
    assertFalse(method.isPUT());
    assertFalse(method.isPOST());
    assertFalse(method.isTRACE());
  }

  @Test
  public void testCONNECT() {
    final HttpMethod method = HttpMethod.from("CONNECT");
    assertTrue(method.isCONNECT());
    
    assertFalse(method.isDELETE());
    assertFalse(method.isGET());
    assertFalse(method.isHEAD());
    assertFalse(method.isOPTIONS());
    assertFalse(method.isPATCH());
    assertFalse(method.isPUT());
    assertFalse(method.isPOST());
    assertFalse(method.isTRACE());
  }
}
