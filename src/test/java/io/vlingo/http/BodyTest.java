// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class BodyTest {
  
  @Test
  public void testThatBodyHasContent() {
    final String content = "{ text:\\\"some text\\\"}\"";
    
    final Body body = Body.from(content);
    
    assertNotNull(body);
    assertNotNull(body.content);
    assertEquals(content, body.content);
  }
  
  @Test
  public void testThatBodyHasNoContent() {
    final Body body = Body.from("");
    
    assertNotNull(body);
    assertNotNull(body.content);
    assertEquals("", body.content);
  }
}
