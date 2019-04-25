package io.vlingo.http.resource;

import io.vlingo.http.MediaType;

import java.util.*;

public class MediaTypeMapper {

  private final Map<MediaType, Mapper> mappersByContentType;

  public MediaTypeMapper(Map<MediaType, Mapper> mappersByContentType) {
    this.mappersByContentType = mappersByContentType;
  }

  public <T> T from(final String data, final MediaType mediaType, final Class<T> type) {
    if (mappersByContentType.containsKey(mediaType)) {
      return mappersByContentType.get(mediaType).from(data, type);
    }
    throw new IllegalStateException("Mapper not found for content-type " + mediaType);
  }

  public <T> String from(final T data, final MediaType mediaType, final Class<T> type) {
    if (mappersByContentType.containsKey(mediaType)) {
      return  mappersByContentType.get(mediaType).from(data);
    }
    throw new MediaTypeNotSupported("Mapper not found for content-type " + mediaType);
  }

  public MediaType[] mappedMediaTypes() {
    return this.mappersByContentType.keySet().toArray(new MediaType[0]);
  }

  public static class Builder {
    private Map<MediaType, Mapper> mappersByContentType;

    public Builder() {
      this.mappersByContentType = new HashMap<>();
    }

    Builder addMapperFor(MediaType mediaType, Mapper mapper) {
      mappersByContentType.computeIfPresent(mediaType,
        (ct, mp) -> {throw new  IllegalArgumentException("Content type already added");});
      mappersByContentType.put(mediaType, mapper);
      return this;
    }

    MediaTypeMapper build() {
      return new MediaTypeMapper(mappersByContentType);
    }
  }
}


