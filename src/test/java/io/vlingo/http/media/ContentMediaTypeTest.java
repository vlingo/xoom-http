package io.vlingo.http.media;

import io.vlingo.http.resource.MediaTypeNotSupportedException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
