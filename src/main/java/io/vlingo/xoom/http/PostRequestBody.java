package io.vlingo.xoom.http;


import io.vlingo.xoom.http.media.ContentMediaType;

import java.util.Objects;

public class PostRequestBody {

  private final Body body;
  private final ContentMediaType mediaType;

  public PostRequestBody(Body body, ContentMediaType mediaType) {
    this.body = body;
    this.mediaType = mediaType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PostRequestBody that = (PostRequestBody) o;
    return Objects.equals(body, that.body) &&
      Objects.equals(mediaType, that.mediaType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(body, mediaType);
  }
}
