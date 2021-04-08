// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.media;

import static io.vlingo.xoom.http.media.MediaTypeDescriptor.PARAMETER_ASSIGNMENT;

import java.util.Arrays;

public class MediaTypeParser {

  private static final int MIME_TYPE_AND_SUBTYPE_SIZE = 2;
  private static final int PARAMETER_VALUE_OFFSET = 1;
  private static final int PARAMETER_FIELD_OFFSET = 0;
  private static final int PARAMETER_AND_VALUE_SIZE = 2;

  public static <T extends MediaTypeDescriptor> T parseFrom(final String mediaTypeDescriptor,
                                                            final MediaTypeDescriptor.Builder<T> builder) {
    String[] descriptorParts = mediaTypeDescriptor.split(MediaTypeDescriptor.PARAMETER_SEPARATOR);
    if (descriptorParts.length > 1) {
      parseAttributes(builder, Arrays.copyOfRange(descriptorParts, 1, descriptorParts.length));
    }

    String[] mimeParts = descriptorParts[0].split(MediaTypeDescriptor.MIME_SUBTYPE_SEPARATOR);
    if (mimeParts.length == MIME_TYPE_AND_SUBTYPE_SIZE) {
      builder.withMimeType(mimeParts[0].trim())
        .withMimeSubType(mimeParts[1].trim());
    }
    return builder.build();
  }

  private static <T extends MediaTypeDescriptor> void parseAttributes(final MediaTypeDescriptor.Builder<T> builder,
                                                                      final String[] parameters) {
      for (String parameter : parameters) {
        String[] parameterFieldAndValue = parameter.split(PARAMETER_ASSIGNMENT);

        if (parameterFieldAndValue.length == PARAMETER_AND_VALUE_SIZE) {
          String attributeName = parameterFieldAndValue[PARAMETER_FIELD_OFFSET];
          String value = parameterFieldAndValue[PARAMETER_VALUE_OFFSET];
          builder.withParameter(attributeName, value);
        }
      }
  }
}
