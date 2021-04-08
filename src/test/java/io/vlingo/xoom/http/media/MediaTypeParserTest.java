// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.media;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

public class MediaTypeParserTest {

  public MediaTypeParserTest() {
  }

  private MediaTypeTest parse(String descriptor) {
    return MediaTypeParser.parseFrom(descriptor, new MediaTypeDescriptor.Builder<>(MediaTypeTest::new));
  }

  @Test
  public void simpleTypeEmptyParameters() {
    MediaTypeTest mediaType = parse("application/json");
    MediaTypeTest mediaTypeExpected = new MediaTypeDescriptor.Builder<>(MediaTypeTest::new)
      .withMimeType("application")
      .withMimeSubType("json")
      .build();

    assertEquals(mediaTypeExpected, mediaType);
  }

  @Test
  public void parseParameters() {
    MediaTypeTest mediaTypeDescriptor = parse("application/*;q=0.8;foo=bar");

    MediaTypeTest mediaTypeExpected = new MediaTypeDescriptor.Builder<>(MediaTypeTest::new)
      .withMimeType("application")
      .withMimeSubType("*")
      .withParameter("q", "0.8")
      .withParameter("foo", "bar")
      .build();

    assertEquals(mediaTypeExpected, mediaTypeDescriptor);
    assertEquals("application/*;q=0.8;foo=bar", mediaTypeDescriptor.toString());
  }

  @Test
  public void incorrectFormatUsesEmptyStringAndDefaultQuality() {
    MediaTypeTest mediaType = parse("typeOnly");
    MediaTypeTest mediaTypeExpected = new MediaTypeDescriptor.Builder<>(MediaTypeTest::new)
      .withMimeType("").withMimeSubType("")
      .build();

    assertEquals(mediaTypeExpected, mediaType);
  }

  static class MediaTypeTest extends MediaTypeDescriptor {

    public MediaTypeTest(String mimeType, String mimeSubType, Map<String, String> parameters) {
      super(mimeType, mimeSubType, parameters);
    }

  }
}
