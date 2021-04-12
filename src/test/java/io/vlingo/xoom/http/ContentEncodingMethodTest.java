// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http;

import org.junit.Test;

import java.util.Optional;
import static org.junit.Assert.*;

public class ContentEncodingMethodTest {

  @Test
  public void methodParseReturnsMethod() {
    String method = "gzip";
    Optional<ContentEncodingMethod> result = ContentEncodingMethod.parse(method);
    assertTrue(result.isPresent());
    assertEquals(ContentEncodingMethod.GZIP, result.get());
  }

  @Test
  public void methodParseReturnsEmpty() {
    String method = "jarjar";
    Optional<ContentEncodingMethod> result = ContentEncodingMethod.parse(method);
    assertFalse(result.isPresent());
  }

}
