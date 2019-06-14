package io.vlingo.http.media;

import io.vlingo.http.media.ResponseMediaTypeSelector.AcceptMediaType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AcceptMediaTypeTest {

  @Test
  public void specificMimeTypeGreaterThanGeneric() {
    AcceptMediaType acceptMediaType1 = new AcceptMediaType("application", "json");
    AcceptMediaType acceptMediaType2 = new AcceptMediaType("*", "*");
    assertEquals( 1, acceptMediaType1.compareTo(acceptMediaType2));
    assertEquals( -1, acceptMediaType2.compareTo(acceptMediaType1));
  }

  @Test
  public void specificMimeSubTypeGreaterThanGeneric() {
    AcceptMediaType acceptMediaType1 = new AcceptMediaType("application", "json");
    AcceptMediaType acceptMediaType2 = new AcceptMediaType("application", "*");
    assertEquals( 1, acceptMediaType1.compareTo(acceptMediaType2));
    assertEquals( -1, acceptMediaType2.compareTo(acceptMediaType1));
  }

  @Test
  public void specificParameterGreaterThanGenericWithSameQualityFactor() {
    AcceptMediaType acceptMediaType1 = new MediaTypeDescriptor.Builder<>(AcceptMediaType::new)
      .withMimeType("application").withMimeSubType("xml").withParameter("version", "1.0")
      .build();

    AcceptMediaType acceptMediaType2 = new AcceptMediaType("application", "json");
    assertEquals( 1, acceptMediaType1.compareTo(acceptMediaType2));
    assertEquals( -1, acceptMediaType2.compareTo(acceptMediaType1));
  }

  @Test
  public void qualityFactorTrumpsSpecificity() {
    AcceptMediaType acceptMediaType1 = new MediaTypeDescriptor.Builder<>(AcceptMediaType::new)
      .withMimeType("text").withMimeSubType("*")
      .build();

    AcceptMediaType acceptMediaType2 = new MediaTypeDescriptor.Builder<>(AcceptMediaType::new)
      .withMimeType("text").withMimeSubType("json")
      .withParameter("q", "0.8")
      .build();

    assertEquals( 1, acceptMediaType1.compareTo(acceptMediaType2));
    assertEquals( -1, acceptMediaType2.compareTo(acceptMediaType1));
  }
}
