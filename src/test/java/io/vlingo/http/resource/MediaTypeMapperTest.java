package io.vlingo.http.resource;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.vlingo.http.media.ContentMediaType;

public class MediaTypeMapperTest {

  private class TestMapper<T> implements Mapper {

    String returnString;
    T returnObject;

    public TestMapper(T mappedToObject, String mappedToString) {
      this.returnObject = mappedToObject;
      this.returnString = mappedToString;
    }

    @Override
    @SuppressWarnings({ "hiding", "unchecked" })
    public <T> T from(String data, Class<T> type) {
      return (T)returnObject;
    }

    @Override
    @SuppressWarnings("hiding")
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
      .addMapperFor(ContentMediaType.Json(), testMapper)
      .build();

    assertEquals(mappedToString, mediaTypeMapper.from(new Object(), ContentMediaType.Json(), Object.class));
  }

  @Test(expected = MediaTypeNotSupportedException.class)
  public void exception_thrown_for_invalid_mapper() {
    MediaTypeMapper mediaTypeMapper = new MediaTypeMapper.Builder()
      .build();
    mediaTypeMapper.from(new Object(), ContentMediaType.Json(), Object.class);
  }

  @Test
  public void parameters_do_not_affect_mapping() {
    MediaTypeMapper mediaTypeMapper = new MediaTypeMapper.Builder()
      .addMapperFor(ContentMediaType.Json(), DefaultJsonMapper.instance)
      .build();
    ContentMediaType contentMediaType = ContentMediaType.parseFromDescriptor("application/json; charset=UTF-8");

    mediaTypeMapper.from(new Object(), contentMediaType, Object.class);
  }

}
