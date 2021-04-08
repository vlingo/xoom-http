package io.vlingo.xoom.http.resource;

public class MediaTypeNotSupportedException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public final String mediaType;

  public MediaTypeNotSupportedException(String mediaType) {
    this.mediaType = mediaType;
  }

  @Override
  public String getMessage() {
    return "No mapper registered for the following media mimeType: " + mediaType;
  }
}
