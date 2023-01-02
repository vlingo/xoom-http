// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.media;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class MediaTypeDescriptor {

  static final String PARAMETER_SEPARATOR = ";";
  static final String MIME_SUBTYPE_SEPARATOR = "/";
  static final String PARAMETER_ASSIGNMENT = "=";

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
        .append(PARAMETER_ASSIGNMENT)
        .append(parameters.get(parameterName));
    }
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MediaTypeDescriptor that = (MediaTypeDescriptor) o;
    return Objects.equals(mimeType, that.mimeType) &&
      Objects.equals(mimeSubType, that.mimeSubType) &&
      Objects.equals(parameters, that.parameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mimeType, mimeSubType, parameters);
  }

  public static class Builder<T> {
    protected String mimeType;
    protected String mimeSubType;
    protected Map<String, String> parameters;
    protected final Supplier<T> supplier;

    @FunctionalInterface
    public interface Supplier<U> {
      U supply(final String mimeType, final String mimeSubType, final Map<String, String> parameters);
    }

    public Builder(Supplier<T> supplier) {
      this.supplier = supplier;
      parameters = new HashMap<>();
      mimeType = "";
      mimeSubType = "";
    }

    Builder<T> withMimeType(final String mimeType) {
      this.mimeType = mimeType;
      return this;
    }

    Builder<T> withMimeSubType(final String mimeSubType) {
      this.mimeSubType = mimeSubType;
      return this;
    }

    Builder<T> withParameter(final String paramName, final String paramValue) {
      parameters.put(paramName, paramValue);
      return this;
    }

    T build() {
      return this.supplier.supply(mimeType, mimeSubType, parameters);
    }
  }
}
