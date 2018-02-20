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

public class HttpVersionTest {

  @Test
  public void testVersion1Dot1() {
    final HttpVersion version = HttpVersion.from("HTTP/1.1");
    
    assertTrue(version.isHttp1_1());
    assertFalse(version.isHttp2_0());
  }

  @Test
  public void testVersion2Dot0() {
    final HttpVersion version = HttpVersion.from("HTTP/2.0");
    
    assertTrue(version.isHttp2_0());
    assertFalse(version.isHttp1_1());
  }

  @Test(expected=IllegalArgumentException.class)
  public void testUnsupportedVersion1Dot0() {
    HttpVersion.from("HTTP/1.0");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testUnsupportedVersion2Dot1() {
    HttpVersion.from("HTTP/2.1");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testUnsupportedGarbage() {
    HttpVersion.from("Blah/Blah");
  }
}
