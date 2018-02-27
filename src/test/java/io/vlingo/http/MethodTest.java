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

public class MethodTest {

  @Test
  public void testPOST() {
    final Method method = Method.from("POST");
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
    final Method method = Method.from("GET");
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
    final Method method = Method.from("PUT");
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
    final Method method = Method.from("PATCH");
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
    final Method method = Method.from("DELETE");
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
    final Method method = Method.from("HEAD");
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
    final Method method = Method.from("TRACE");
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
    final Method method = Method.from("OPTIONS");
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
    final Method method = Method.from("CONNECT");
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
