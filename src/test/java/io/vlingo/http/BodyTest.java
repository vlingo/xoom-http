// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class BodyTest {
  
  @Test
  public void testThatBodyHasLines() {
    final Body body = Body.from(Arrays.asList("line1", "line2", "line3"));
    
    assertTrue(body.hasLines());
    assertEquals(3, body.lines.size());
    assertEquals("line1", body.line(0));
    assertEquals("line2", body.line(1));
    assertEquals("line3", body.line(2));
  }
  
  @Test
  public void testThatBodyHasNoLines() {
    final Body body = Body.from(Arrays.asList());
    
    assertFalse(body.hasLines());
  }
}
