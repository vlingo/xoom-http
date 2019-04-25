package io.vlingo.http.resource;

import io.vlingo.http.MediaType;
import org.junit.Test;

import static org.junit.Assert.*;

public class MediaTypeMapperTest {

  private class TestMapper<T> implements Mapper {

    String returnString;
    T returnObject;

    public TestMapper(T mappedToObject, String mappedToString) {
      this.returnObject = mappedToObject;
      this.returnString = mappedToString;
    }

    @Override
    public <T> T from(String data, Class<T> type) {
      return (T)returnObject;
    }

    @Override
    public <T> String from(T data) {
      return returnString;
    }
  }

  @Test
  public void registered_mapper_maps_type() {
    final Object mappedToObject = new Object();
    final String mappedToString = "mappedToString";

    TestMapper<Object> testMapper = new TestMapper<>(mappedToObject, mappedToString);
    MediaTypeMapper mediaTypeMapper = new MediaTypeMapper.Builder()
      .addMapperFor(MediaType.Json(), testMapper)
      .build();

    Content expectedContent = new Content(mappedToString, MediaType.Json());
    assertEquals(expectedContent, mediaTypeMapper.from(new Object(), MediaType.Json(), Object.class));
  }

  @Test(expected = MediaTypeNotSupported.class)
  public void exception_thrown_for_invalid_mapper() {
    MediaTypeMapper mediaTypeMapper = new MediaTypeMapper.Builder()
      .build();
    mediaTypeMapper.from(new Object(), MediaType.Json(), Object.class);
  }

}
