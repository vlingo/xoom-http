package io.vlingo.http;

import io.vlingo.http.resource.MediaTypeNotSupported;

import java.util.Iterator;
import java.util.TreeSet;

public class ResponseMediaTypeSelector {

  private final static MediaType DEFAULT_CONTENT_TYPE = MediaType.Json();

  private final TreeSet<MediaTypeDescriptor> mediaTypeOrderByWeight;
  private final String mediaTypeDescriptors;

  public ResponseMediaTypeSelector(String mediaTypeDescriptors) {
    this.mediaTypeOrderByWeight = new TreeSet<>();
    this.mediaTypeDescriptors = mediaTypeDescriptors;
    parseMediaTypeDescriptors(mediaTypeDescriptors);
  }

  private void parseMediaTypeDescriptors(String contentTypeList) {
    String[] acceptedContentTypeDescriptors = contentTypeList.split(",");

    for (String acceptedContentTypeDescriptor : acceptedContentTypeDescriptors) {
      MediaTypeDescriptor mediaTypeDescriptor = MediaTypeDescriptor.parseFrom(acceptedContentTypeDescriptor);
        mediaTypeOrderByWeight.add(mediaTypeDescriptor);
    }
  }

  public MediaType selectType(MediaType[] mediaTypes) {
    Iterator<MediaTypeDescriptor> iterator = mediaTypeOrderByWeight.descendingIterator();
    while (iterator.hasNext()) {
      MediaTypeDescriptor mediaTypeDescriptor = iterator.next();
      for (MediaType mediaType : mediaTypes) {
        if (mediaTypeDescriptor.isSameOrSuperType(mediaType)) {
          return mediaType;
        }
      }
    }
    throw new MediaTypeNotSupported(mediaTypeDescriptors);
  }


}
