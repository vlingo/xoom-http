package io.vlingo.http;

import io.vlingo.http.resource.MediaTypeNotSupported;

import java.util.Iterator;
import java.util.TreeSet;

public class ResponseMediaTypeSelector {
  //todo: use json if nothing found... why is this unused?
  private final static MediaType DEFAULT_CONTENT_TYPE = MediaType.Json();
  public static final String MEDIA_TYPE_SEPARATOR = ",";

  private final TreeSet<AcceptMediaType> responseMediaTypesByPriority;
  private final String mediaTypeDescriptors;

  public ResponseMediaTypeSelector(String mediaTypeDescriptors) {
    this.responseMediaTypesByPriority = new TreeSet<>();
    this.mediaTypeDescriptors = mediaTypeDescriptors;
    parseMediaTypeDescriptors(mediaTypeDescriptors);
  }

  private void parseMediaTypeDescriptors(String contentTypeList) {
    String[] acceptedContentTypeDescriptors = contentTypeList.split(MEDIA_TYPE_SEPARATOR);
    MediaTypeParser parser = new MediaTypeParser();

    for (String acceptedContentTypeDescriptor : acceptedContentTypeDescriptors) {
      AcceptMediaType acceptMediaType = parser.parseFrom(acceptedContentTypeDescriptor.trim(),
        new MediaTypeDescriptor.Builder<>(AcceptMediaType::new));
      responseMediaTypesByPriority.add(acceptMediaType);
    }
  }

  public MediaType selectType(MediaType[] supportedMediaTypes) {
    Iterator<AcceptMediaType> iteratorMediaTypeCandidates = responseMediaTypesByPriority.descendingIterator();
    while (iteratorMediaTypeCandidates.hasNext()) {
      AcceptMediaType responseMediaType = iteratorMediaTypeCandidates.next();
      for (MediaType supportedMediaType : supportedMediaTypes) {
        if (responseMediaType.isSameOrSuperTypeOf(supportedMediaType)) {
          return supportedMediaType;
        }
      }
    }
    throw new MediaTypeNotSupported(mediaTypeDescriptors);
  }

}
