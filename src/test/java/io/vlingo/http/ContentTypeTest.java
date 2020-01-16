/*
 * Copyright © 2012-2020 VLINGO LABS. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http;

import org.junit.Assert;
import org.junit.Test;

public class ContentTypeTest {

  @Test
  public void testThatContentTypeHasMediaTypeOnly() {
    final ContentType contentType = ContentType.of("text/html");

    Assert.assertNotNull(contentType);
    Assert.assertEquals("text/html", contentType.mediaType);
    Assert.assertEquals("text/html", contentType.toString());
    Assert.assertTrue(contentType.charset.isEmpty());
    Assert.assertTrue(contentType.boundary.isEmpty());
  }

  @Test
  public void testThatContentTypeHasMediaTypeCharsetOnly() {
    final ContentType contentType = ContentType.of("text/html", "charset=UTF-8");

    Assert.assertNotNull(contentType);
    Assert.assertEquals("text/html", contentType.mediaType);
    Assert.assertEquals("charset=UTF-8", contentType.charset);
    Assert.assertEquals("text/html; charset=UTF-8", contentType.toString());
    Assert.assertTrue(contentType.boundary.isEmpty());
  }

  @Test
  public void testThatContentTypeHasMediaTypeCharsetBoundaryOnly() {
    final ContentType contentType = ContentType.of("text/html", "charset=UTF-8", "boundary=something");

    Assert.assertNotNull(contentType);
    Assert.assertEquals("text/html", contentType.mediaType);
    Assert.assertEquals("charset=UTF-8", contentType.charset);
    Assert.assertEquals("boundary=something", contentType.boundary);
    Assert.assertEquals("text/html; charset=UTF-8; boundary=something", contentType.toString());
  }
}
