package io.vlingo.http.resource;

import io.vlingo.http.MediaType;

import java.util.Objects;


public class Content {
  public final String data;
  public final MediaType mediaType;

  public Content(String data, MediaType mediaType) {
    this.data = data;
    this.mediaType = mediaType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Content content = (Content) o;
    return Objects.equals(data, content.data) &&
      Objects.equals(mediaType, content.mediaType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data, mediaType);
  }
}
