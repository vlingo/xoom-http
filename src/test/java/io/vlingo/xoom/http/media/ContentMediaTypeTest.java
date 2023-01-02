// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.media;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.vlingo.xoom.http.resource.MediaTypeNotSupportedException;

public class ContentMediaTypeTest {

  @Test(expected = MediaTypeNotSupportedException.class)
  public void wildCardsAreNotAllowed() {
    new ContentMediaType("application", "*");
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidMimeTypeNotAllowed() {
   new ContentMediaType("unknownMimeType", "foo");
  }

  @Test
  public void builderCreates() {
    ContentMediaType.Builder<ContentMediaType> builder = new ContentMediaType.Builder<>(ContentMediaType::new);
    ContentMediaType contentMediaType = builder
      .withMimeType(ContentMediaType.mimeTypes.application.name())
      .withMimeSubType("json")
      .build();

    assertEquals(ContentMediaType.Json(), contentMediaType);
  }

  @Test
  public void builtInTypesHaveCorrectFormat() {
    ContentMediaType jsonType = new ContentMediaType("application", "json");
    assertEquals(jsonType, ContentMediaType.Json());

    ContentMediaType xmlType = new ContentMediaType("application", "xml");
    assertEquals(xmlType, ContentMediaType.Xml());
  }
}
