package io.vlingo.http.media;

import io.vlingo.http.resource.MediaTypeNotSupported;

import java.util.Map;

public class ContentMediaType extends MediaTypeDescriptor {

  //IANA MIME Type List
  enum mimeTypes {
    application,
    audio,
    font,
    image,
    model,
    text,
    video,
    multipart,
    message
  }

  public ContentMediaType(final String mediaType, final String mediaSubType) {
    super(mediaType, mediaSubType);
    validate();
  }

  private void validate() {
    mimeTypes.valueOf(mimeType);
    if (mimeSubType.equals("*")) {
      throw new MediaTypeNotSupported("Illegal MIME type:" + toString());
    }
  }

  public ContentMediaType(String mediaType, String mediaSubType, Map<String, String> parameters) {
    super(mediaType, mediaSubType, parameters);
    validate();
  }

  public static ContentMediaType Json() {
    return new ContentMediaType(mimeTypes.application.name(), "json");
  }

  public static ContentMediaType Xml() {
    return new ContentMediaType(mimeTypes.application.name(), "xml");
  }

  public static ContentMediaType parseFromDescriptor(String contentMediaTypeDescriptor) {
    return MediaTypeParser.parseFrom(contentMediaTypeDescriptor,
      new MediaTypeDescriptor.Builder<>(ContentMediaType::new));
  }
}
