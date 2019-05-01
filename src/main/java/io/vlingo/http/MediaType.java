package io.vlingo.http;
import java.util.Map;
import java.util.Objects;

public class MediaType extends MediaTypeDescriptor {

  public String type;
  public String subType;

  public MediaType(String mediaType, String mediaSubType) {
    super(mediaType, mediaSubType);
  }

  public MediaType(String mediaType, String mediaSubType, Map<String, String> parameters) {
    super(mediaType, mediaSubType, parameters);
  }

  public String mimeType() {
    return type + "/" + subType;
  }

  @Override
  public String toString() {
    return mimeType();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MediaType mediaType = (MediaType) o;
    return Objects.equals(type, mediaType.type) &&
      Objects.equals(subType, mediaType.subType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, subType);
  }

  public static MediaType fromMimeTypeDescriptor(String mimeTypeDescriptor) {
    String[] parts = mimeTypeDescriptor.split(MediaTypeDescriptor.MIME_SUBTYPE_SEPARATOR);
    return (parts.length == 2) ?
          fromMimeTypeDescriptor(parts[0], parts[1]) :
          null;
  }

  public static MediaType fromMimeTypeDescriptor(String mediaType, String mediaSubType) {
    return new MediaType(mediaType, mediaSubType);
  }

  public static MediaType Json() {
    return new MediaType("application", "json");
  }

  public static MediaType Xml() {
    return new MediaType("application", "json");
  }


}
