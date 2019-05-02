package io.vlingo.http.resource;

import io.vlingo.http.media.ContentMediaType;

public class DefaultMediaTypeMapper  {

  private static MediaTypeMapper instance = buildInstance();

  private static MediaTypeMapper buildInstance() {
    return new MediaTypeMapper.Builder()
      .addMapperFor(ContentMediaType.Json(), DefaultJsonMapper.instance)
      .build();
  }

  public static MediaTypeMapper instance() {
    return instance;
  }
}
