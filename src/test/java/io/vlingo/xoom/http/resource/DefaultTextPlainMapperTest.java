package io.vlingo.xoom.http.resource;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DefaultTextPlainMapperTest {

  @Test
  public void testFromObjectToStringUsesToString() {
    DefaultTextPlainMapper mapper = new DefaultTextPlainMapper();
    assertEquals("toStringResult", mapper.from(new ObjectForTest()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDeserializationToNonStringFails() {
    DefaultTextPlainMapper mapper = new DefaultTextPlainMapper();
    @SuppressWarnings("unused")
    ObjectForTest cannotBeDeserialized = mapper.from("some string", ObjectForTest.class);
  }

  @Test
  public void testDeserializationToStringSucceed() {
    DefaultTextPlainMapper mapper = new DefaultTextPlainMapper();
    String canBeSerialized = mapper.from("some string", String.class);
    assertEquals("some string", canBeSerialized);
  }

  private static class ObjectForTest {

    @Override
    public String toString() {
      return "toStringResult";
    }
  }
}
