// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.media;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import io.vlingo.xoom.http.resource.MediaTypeNotSupportedException;

public class ResponseMediaTypeSelector {

  private static final String ACCEPT_MEDIA_TYPE_SEPARATOR = ",";

  private final TreeSet<AcceptMediaType> responseMediaTypesByPriority;
  private final String mediaTypeDescriptors;

  public ResponseMediaTypeSelector(final String mediaTypeDescriptors) {
    this.responseMediaTypesByPriority = new TreeSet<>();
    this.mediaTypeDescriptors = mediaTypeDescriptors;
    parseMediaTypeDescriptors(mediaTypeDescriptors);
  }

  private void parseMediaTypeDescriptors(final String contentTypeList) {
    String[] acceptedContentTypeDescriptors = contentTypeList.split(ACCEPT_MEDIA_TYPE_SEPARATOR);

    for (String acceptedContentTypeDescriptor : acceptedContentTypeDescriptors) {
      AcceptMediaType acceptMediaType = MediaTypeParser.parseFrom(acceptedContentTypeDescriptor.trim(),
        new MediaTypeDescriptor.Builder<>(AcceptMediaType::new));
      responseMediaTypesByPriority.add(acceptMediaType);
    }
  }

  public ContentMediaType selectType(final ContentMediaType[] supportedContentMediaTypes) {
    Iterator<AcceptMediaType> iteratorMediaTypeCandidates = responseMediaTypesByPriority.descendingIterator();
    while (iteratorMediaTypeCandidates.hasNext()) {
      AcceptMediaType responseMediaType = iteratorMediaTypeCandidates.next();
      for (ContentMediaType supportedContentMediaType : supportedContentMediaTypes) {
        if (responseMediaType.isSameOrSuperTypeOf(supportedContentMediaType)) {
          return supportedContentMediaType;
        }
      }
    }
    throw new MediaTypeNotSupportedException(mediaTypeDescriptors);
  }


  static class AcceptMediaType extends MediaTypeDescriptor implements Comparable<AcceptMediaType> {

    private static final String MIME_TYPE_WILDCARD = "*";
    private static final String QUALITY_FACTOR_PARAMETER = "q";
    private static final float DEFAULT_QUALITY_FACTOR_VALUE = 1.0f;

    private final float qualityFactor;

    AcceptMediaType(final String mimeType, final String mimeSubType, final Map<String, String> parameters) {
      super(mimeType, mimeSubType, parameters);
      float qualityFactor = DEFAULT_QUALITY_FACTOR_VALUE;

      if (parameters.containsKey(QUALITY_FACTOR_PARAMETER)) {
        try {
          qualityFactor = Float.parseFloat(parameters.get(QUALITY_FACTOR_PARAMETER));
        } catch (NumberFormatException ignored) { }
      }
      this.qualityFactor = qualityFactor;
    }

    AcceptMediaType(final String mimeType, final String mimeSubType) {
      super(mimeType, mimeSubType);
      this.qualityFactor = DEFAULT_QUALITY_FACTOR_VALUE;
    }

    /**
     * Compares two AcceptMediaTypes based on specification by:
     * * quality factor, then
     * * specificity of media type (specific before wildcards), then
     * * specificity of sub-media type (specific before wildcards), then
     * * specificity of parameters (more parameters -> greater specificity)
     *
     * @param other AcceptedMediaType against which to compare
     * @return -1, 0, or 1
     */
    @Override
    public int compareTo(final AcceptMediaType other) {
      if (this.qualityFactor == other.qualityFactor) {
        if (isGenericType() && !other.isGenericType()) {
          return -1;
        } else if (!isGenericType() && other.isGenericType()) {
          return 1;
        } else if (isGenericSubType()) {
          return (other.isGenericSubType() ? compareParameters(other) : -1);
        } else {
          return (other.isGenericSubType() ? 1 : compareParameters(other));
        }
      } else {
        return Float.compare(qualityFactor, other.qualityFactor);
      }
    }

    private int compareParameters(final AcceptMediaType other) {
      return Integer.compare(parameters.size(), other.parameters.size());
    }

    boolean isSameOrSuperTypeOf(final ContentMediaType contentMediaType) {
      return
        (isGenericType() || mimeType.equals(contentMediaType.mimeType))
          && (isGenericSubType() || mimeSubType.equals(contentMediaType.mimeSubType));
    }

    private boolean isGenericSubType() {
      return mimeSubType.equals(MIME_TYPE_WILDCARD);
    }

    private boolean isGenericType() {
      return mimeType.equals(MIME_TYPE_WILDCARD);
    }
  }
}
