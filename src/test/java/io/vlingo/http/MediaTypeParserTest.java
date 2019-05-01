package io.vlingo.http;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class MediaTypeParserTest {

  private final MediaTypeParser parser;

  public MediaTypeParserTest() {
    parser = new MediaTypeParser();
  }

  @Test
  public void simpleTypeDefaultWeight() {
    MediaType mediaType = parser.parseFrom("application/json");
    assertEquals(MediaType.Json(), mediaType);
  }

  @Test
  public void genericTypeSpecificWeight() {
    MediaType mediaType = parser.parseFrom("application/*;q=0.8;foo=bar");

    MediaType mediaTypeExpected = new MediaType.Builder()
      .withMimeType("application").withMimeSubType("*")
      .withAttribute(MediaType.QUALITY_FACTOR_PARAMETER, "0.8")
      .withAttribute("foo", "bar")
      .build();

    assertEquals(mediaTypeExpected, mediaType);
  }

  @Test
  public void incorrectFormatUsesEmptyStringAndDefaultQuality() {
    MediaType mediaType = parser.parseFrom("typeOnly");
    MediaType mediaTypeExpected = new MediaType.Builder()
      .withMimeType("").withMimeSubType("")
      .build();

    assertEquals(mediaTypeExpected, mediaType);
  }

  @Test
  @Ignore
  public void specificDescriptorGreaterThanGeneric() {
    MediaType mediaType1 = parser.parseFrom("application/*");
    MediaType mediaType2 = parser.parseFrom("application/json");
    //assertEquals( 1, mediaType1.compareTo(descriptorGeneric));
  }

  @Test
  @Ignore
  public void specificDescriptorAttributeGreaterThanGeneric() {
    // This case is not yet implemented; un-needed feature perhaps?
    MediaType specificParam = parser.parseFrom("application/json;param=1");
    MediaType specific = parser.parseFrom("application/json");
    //assertEquals( 1, descriptorSpecificParam.compareTo(descriptorSpecific));
  }

}
