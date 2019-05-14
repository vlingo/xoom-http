package io.vlingo.http.resource;

public class MediaTypeNotSupportedException extends RuntimeException {
  public final String mediaType;

  public MediaTypeNotSupportedException(String mediaType) {
    this.mediaType = mediaType;
  }

  @Override
  public String getMessage() {
    return "No mapper registered for the following media mimeType: " + mediaType;
  }
}
