// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import java.util.Arrays;
import java.util.List;

public class StaticFilesConfiguration {
  private int poolSize;
  private String rootPath;
  private List<String> subpaths;

  public static StaticFilesConfiguration defineWith(final int poolSize, final String rootPath, final List<String> subpaths) {
    return new StaticFilesConfiguration(poolSize, rootPath, subpaths);
  }

  public static StaticFilesConfiguration define() {
    return new StaticFilesConfiguration(0, "", Arrays.asList());
  }

  public StaticFilesConfiguration with(final int poolSize) {
    this.poolSize = poolSize;
    return this;
  }

  public StaticFilesConfiguration with(final String rootPath) {
    this.rootPath = rootPath;
    return this;
  }

  public StaticFilesConfiguration with(final List<String> subpaths) {
    this.subpaths = subpaths;
    return this;
  }

  public int poolSize() {
    return poolSize;
  }

  public String rootPath() {
    return rootPath;
  }

  public List<String> subpaths() {
    return subpaths;
  }

  public String[] subpathsAsArray() {
    return subpaths.toArray(new String[subpaths.size()]);
  }

  public String subpathsAsPropertyValue() {
    final StringBuilder builder = new StringBuilder();

    builder.append("[");

    String separator = "";

    for (final String path : subpaths) {
      builder.append(separator).append(path);
      separator = ", ";
    }

    builder.append("]");

    return builder.toString();
  }

  public boolean isConfigured() {
    return poolSize > 0 && !rootPath.isEmpty() && !subpaths.isEmpty();
  }

  private StaticFilesConfiguration(final int poolSize, final String rootPath, final List<String> subpaths) {
    this.poolSize = poolSize;
    this.rootPath = rootPath;
    this.subpaths = subpaths;
  }
}
