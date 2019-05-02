package io.vlingo.http.resource;

import io.vlingo.http.media.ContentMediaType;

import java.util.Objects;


public class Content {
  public final String data;
  public final ContentMediaType contentMediaType;

  public Content(String data, ContentMediaType contentMediaType) {
    this.data = data;
    this.contentMediaType = contentMediaType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Content content = (Content) o;
    return Objects.equals(data, content.data) &&
      Objects.equals(contentMediaType, content.contentMediaType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data, contentMediaType);
  }
}
