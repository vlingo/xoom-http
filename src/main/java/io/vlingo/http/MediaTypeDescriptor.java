package io.vlingo.http;

import java.util.HashMap;
import java.util.Map;

public abstract class MediaTypeDescriptor {

  static final String PARAMETER_SEPARATOR = ";";
  static final String MIME_SUBTYPE_SEPARATOR = "/";
  static final String PARAMETER_ASSIGNMENT = "=";
  static final String MIME_TYPE_WILDCARD = "*";

  protected final String mimeType;
  protected final String mimeSubType;
  public final Map<String, String> parameters;

  public MediaTypeDescriptor(String mimeType, String mimeSubType, Map<String, String> parameters) {
    this.mimeType = mimeType;
    this.mimeSubType = mimeSubType;
    this.parameters = new HashMap<>(parameters);
  }

  public MediaTypeDescriptor(String mimeType, String mimeSubType) {
    this.mimeType = mimeType;
    this.mimeSubType = mimeSubType;
    this.parameters = new HashMap<>();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(mimeType)
      .append(MIME_SUBTYPE_SEPARATOR)
      .append(mimeSubType);

    for (String parameterName : parameters.keySet()) {
      sb.append(PARAMETER_SEPARATOR)
        .append(parameterName)
        .append(PARAMETER_SEPARATOR)
        .append(parameters.get(parameterName));
    }
    return sb.toString();
  }

  static class Builder <T> {
    String mimeType;
    String mimeSubType;
    Map<String, String> parameters;
    private Supplier<T> supplier;

    @FunctionalInterface
    interface Supplier <T> {
      T supply(String mimeType, String mimeSubType, Map<String, String> parameters);
    }

    public Builder(Supplier supplier) {
      this.supplier = supplier;
      parameters = new HashMap<>();
      mimeType = "";
      mimeSubType = "";
    }

    Builder withMimeType(String mimeType) { this.mimeType = mimeType; return this;}

    Builder withMimeSubType(String mimeSubType) {this.mimeSubType = mimeSubType; return this;}

    Builder withParameter(String paramName, String paramValue) {
      parameters.put(paramName, paramValue);
      return this;
    }

    T build() {
      return supplier.supply(mimeType, mimeSubType, parameters);
    }
  }
}
