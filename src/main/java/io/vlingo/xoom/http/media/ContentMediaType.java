// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.media;

import java.util.Map;

import io.vlingo.xoom.http.resource.MediaTypeNotSupportedException;

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
      throw new MediaTypeNotSupportedException("Illegal MIME type:" + toString());
    }
  }

  public ContentMediaType(String mediaType, String mediaSubType, Map<String, String> parameters) {
    super(mediaType, mediaSubType, parameters);
    validate();
  }

  public ContentMediaType toBaseType() {
    if (parameters.isEmpty()) {
      return this;
    }
    return new ContentMediaType(mimeType, mimeSubType);
  }

  public static ContentMediaType Json() {
    return new ContentMediaType(mimeTypes.application.name(), "json");
  }

  public static ContentMediaType Xml() {
    return new ContentMediaType(mimeTypes.application.name(), "xml");
  }

  public static  ContentMediaType PlainText() { return new ContentMediaType(mimeTypes.text.name(), "plain"); }

  public static ContentMediaType BinaryContent() { return new ContentMediaType(mimeTypes.application.name(), "octet-stream"); }

  public static ContentMediaType CompressedZipContent() { return new ContentMediaType(mimeTypes.application.name(), "gzip"); }

  public static ContentMediaType CompressedTarContent() { return new ContentMediaType(mimeTypes.application.name(), "octet-stream"); }


  public static ContentMediaType parseFromDescriptor(String contentMediaTypeDescriptor) {
    return MediaTypeParser.parseFrom(contentMediaTypeDescriptor,
      new MediaTypeDescriptor.Builder<>(ContentMediaType::new));
  }
}
