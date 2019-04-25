package io.vlingo.http.resource;

public class MediaTypeNotSupported extends RuntimeException {
  public final String mediaType;

  public MediaTypeNotSupported(String mediaType) {
    this.mediaType = mediaType;
  }

  @Override
  public String getMessage() {
    return "No mapper registered for the following media type: " + mediaType;
  }
}
