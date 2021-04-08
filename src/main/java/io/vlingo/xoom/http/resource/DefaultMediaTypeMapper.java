package io.vlingo.xoom.http.resource;

import io.vlingo.xoom.http.media.ContentMediaType;

public class DefaultMediaTypeMapper  {

  private static MediaTypeMapper instance = buildInstance();

  private DefaultMediaTypeMapper() {
    // no-op
  }

  private static MediaTypeMapper buildInstance() {
    return new MediaTypeMapper.Builder()
      .addMapperFor(ContentMediaType.Json(), DefaultJsonMapper.instance)
      .build();
  }

  public static MediaTypeMapper instance() {
    return instance;
  }
}
