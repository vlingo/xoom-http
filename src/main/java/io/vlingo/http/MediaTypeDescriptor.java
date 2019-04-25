package io.vlingo.http;

import java.util.Arrays;

public class MediaTypeDescriptor implements Comparable<MediaTypeDescriptor> {

  private static final int MIME_TYPE_AND_SUBTYPE_SIZE = 2;
  private static final String PARAMETER_SEPARATOR = ";";
  private static final String MIME_SUBTYPE_SEPARATOR = "/";

  private static final int PARAMETER_VALUE_OFFSET = 1;
  private static final int PARAMETER_FIELD_OFFSET = 0;
  private static final int PARAMETER_AND_VALUE_SIZE = 2;
  private static final String PARAMETER_ASSIGNMENT = "=";
  private static final String QUALITY_FIELD_NAME = "q";


  public final String mimeType;
  public final String mimeSubType;
  public final float weight;

  private MediaTypeDescriptor(String mimeType, String mimeSubType, float weight) {
    this.mimeType = mimeType;
    this.mimeSubType = mimeSubType;
    this.weight = weight;
  }

  public static MediaTypeDescriptor parseFrom(String contentTypeDescriptor) {

    Builder builder = new Builder();
    String[] descriptorParts = contentTypeDescriptor.split(PARAMETER_SEPARATOR);
    if (descriptorParts.length > 1) {
      parseAttributes(builder, Arrays.copyOfRange(descriptorParts,1, descriptorParts.length));
    }

    String[] mimeParts = descriptorParts[0].split(MIME_SUBTYPE_SEPARATOR);
    if (mimeParts.length == MIME_TYPE_AND_SUBTYPE_SIZE) {
      builder.withMimeType(mimeParts[0].trim())
             .withMimeSubType(mimeParts[1].trim());
    }

    return builder.build();
  }

  private static void parseAttributes(Builder builder, String[] parameters) {
      builder.withWeight(1.0f);
      for (String parameter : parameters) {
        String[] parameterFieldAndValue = parameter.split(PARAMETER_ASSIGNMENT);

        if (parameterFieldAndValue.length == PARAMETER_AND_VALUE_SIZE) {
          String field = parameterFieldAndValue[PARAMETER_FIELD_OFFSET];
          String value = parameterFieldAndValue[PARAMETER_VALUE_OFFSET];
          if (field.trim().equals(QUALITY_FIELD_NAME)) {
            try {
              builder.withWeight(Float.parseFloat(value));
            } catch (NumberFormatException ignored) {
            }
          }
        }
      }
  }
  
  @Override
  public String toString() {
    return
      mimeType + MIME_SUBTYPE_SEPARATOR + mimeSubType
        + PARAMETER_SEPARATOR +
        QUALITY_FIELD_NAME + PARAMETER_ASSIGNMENT + Float.toString(weight);
  }

  public boolean isGenericSubType() {
    return mimeSubType.equals("*");
  }
  
  @Override
  public int compareTo(MediaTypeDescriptor o) {
    if (o.weight == this.weight) {
      if (o.mimeType.equals(mimeType)) {
        if (isGenericSubType()) {
          return (o.isGenericSubType() ? 0 : 1);
        } else {
          return (o.isGenericSubType() ? 1 : 0);
        }
      }
      else {
        // in case of a tie, alphabetic order determines precedence
        return mimeType.compareTo(o.mimeType);
      }
    }
    else
      return (Float.compare(this.weight, o.weight));
  }

  public boolean isSameOrSuperType(MediaType mediaType) {
    return
         (this.mimeType.equals("*") || this.mimeType.equals(mediaType.type))
      && (this.mimeSubType.equals("*") || this.mimeSubType.equals(mediaType.subType));
  
  }

  static class Builder {
    String mimeType;
    String mimeSubType;
    float weight;

    public Builder() {
      weight = 1.0f;
      mimeType = "";
      mimeSubType = "";
    }

    Builder withMimeType(String mimeType) { this.mimeType = mimeType; return this;}
    Builder withMimeSubType(String mimeSubType) {this.mimeSubType = mimeSubType; return this;}
    Builder withWeight(float weight) {this.weight = weight; return this;}
    MediaTypeDescriptor build() {
      return new MediaTypeDescriptor(mimeType, mimeSubType, weight);
    }
  }
}
