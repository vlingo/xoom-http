package io.vlingo.http;


import java.util.Objects;

public class MediaType {

  public String type;
  public String subType;

  private MediaType(String type, String subType) {
    this.type = type; this.subType = subType;
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

  public static MediaType fromMimeType(String mediaTypeDescrriptor) {
    String[] parts = mediaTypeDescrriptor.split("/");
    return (parts.length == 2) ?
          fromMimeType(parts[0], parts[1]) :
          null;
  }

  public static MediaType fromMimeType(String mediaType, String mediaSubType) {
    return new MediaType(mediaType, mediaSubType);
  }

  public static MediaType Json() {
    return new MediaType("application", "json");
  }

  public static MediaType Xml() {
    return new MediaType("application", "json");
  }


}
