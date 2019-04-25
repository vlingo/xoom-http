package io.vlingo.http;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

public class MediaTypeDescriptorTest {

  @Test
  public void simpleTypeDefaultWeight() {
    MediaTypeDescriptor descriptor = MediaTypeDescriptor.parseFrom("application/json");
    assertEquals("application", descriptor.mimeType);
    assertEquals("json", descriptor.mimeSubType);
    assertEquals(1.0f, descriptor.weight, 0.0f);
  }

  @Test
  public void genericTypeSpecificWeight() {
    MediaTypeDescriptor descriptor = MediaTypeDescriptor.parseFrom("application/*;q=0.8");
    assertEquals("application", descriptor.mimeType);
    assertEquals("*", descriptor.mimeSubType);
    assertEquals(0.8f, descriptor.weight, 0.0f);
  }

  @Test
  public void specificTypeAdditionalParameters() {
    MediaTypeDescriptor descriptor = MediaTypeDescriptor.parseFrom("application/xml;q=0.5;foo=bar");
    assertEquals("application", descriptor.mimeType);
    assertEquals("xml", descriptor.mimeSubType);
    assertEquals(0.5f, descriptor.weight, 0.0f);
  }

  @Test
  public void incorrectFormatUsesEmptyStringAndDefaultQuality() {
    MediaTypeDescriptor descriptor = MediaTypeDescriptor.parseFrom("application;q=awesome");
    assertEquals("", descriptor.mimeType);
    assertEquals("", descriptor.mimeSubType);
    assertEquals(1.0f, descriptor.weight, 0.0f);
  }

  @Test
  public void specificDescriptorGreaterThanGeneric() {
    MediaTypeDescriptor descriptorGeneric = MediaTypeDescriptor.parseFrom("application/*");
    MediaTypeDescriptor descriptorSpecific = MediaTypeDescriptor.parseFrom("application/json");
    assertEquals( 1, descriptorSpecific.compareTo(descriptorGeneric));
  }

  @Test
  @Ignore
  public void specificDescriptorAttributeGreaterThanGeneric() {
    // This case is not yet implemented; un-needed feature perhaps?
    MediaTypeDescriptor descriptorSpecificParam = MediaTypeDescriptor.parseFrom("application/json;param=1");
    MediaTypeDescriptor descriptorSpecific = MediaTypeDescriptor.parseFrom("application/json");
    assertEquals( 1, descriptorSpecificParam.compareTo(descriptorSpecific));
  }

}
