package io.vlingo.http;

import java.util.Arrays;

import static io.vlingo.http.MediaTypeDescriptor.PARAMETER_ASSIGNMENT;

public class MediaTypeParser {

  private static final int MIME_TYPE_AND_SUBTYPE_SIZE = 2;
  private static final int PARAMETER_VALUE_OFFSET = 1;
  private static final int PARAMETER_FIELD_OFFSET = 0;
  private static final int PARAMETER_AND_VALUE_SIZE = 2;

  public static <T extends MediaTypeDescriptor> T parseFrom(String mediaTypeDescriptor, MediaTypeDescriptor.Builder<T> builder) {
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

  public static MediaType parseToMediaType(String mediaTypeDescriptor) {
   return parseFrom(mediaTypeDescriptor,
     new MediaTypeDescriptor.Builder<>(MediaType::new));
  }

  private static <T extends MediaTypeDescriptor> void parseAttributes(MediaTypeDescriptor.Builder<T> builder, String[] parameters) {
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
