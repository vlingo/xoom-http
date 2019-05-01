package io.vlingo.http;

import java.util.HashMap;
import java.util.Map;

// todo: Make into an inner class and hide so not exposed to others
class AcceptMediaType extends MediaTypeDescriptor implements Comparable<AcceptMediaType> {

  static final String QUALITY_FACTOR_PARAMETER = "q";
  private static final String DEFAULT_QUALITY_FACTOR = "1.0";
  private final float qualityFactor;

  protected AcceptMediaType(String mimeType, String mimeSubType, Map<String, String> parameters) {
    super(mimeType, mimeSubType, parameters);
    this.qualityFactor = Float.parseFloat(parameters.getOrDefault(QUALITY_FACTOR_PARAMETER, DEFAULT_QUALITY_FACTOR));
  }

  @Override
  public int compareTo(AcceptMediaType o) {
    if (o.qualityFactor == this.qualityFactor) {
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
      return (Float.compare(this.qualityFactor, o.qualityFactor));
  }

  public boolean isSameOrSuperTypeOf(MediaType other) {
    return
      (isGenericType() || mimeType.equals(other.mimeType))
        && (isGenericSubType() || mimeSubType.equals(other.mimeSubType));
  }

  public boolean isGenericSubType() {
    return mimeSubType.equals(MIME_TYPE_WILDCARD);
  }

  public boolean isGenericType() {
    return mimeType.equals(MIME_TYPE_WILDCARD);
  }
}
