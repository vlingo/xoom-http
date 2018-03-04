// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.io.IOException;

public class Properties {
  private static final String propertiesFile = "/vlingo-http.properties";
  
  private final String name;
  private final java.util.Properties properties;

  static java.util.Properties loadProperties() {
    final java.util.Properties properties = new java.util.Properties();

    try {
      properties.load(Properties.class.getResourceAsStream(propertiesFile));
    } catch (IOException e) {
      throw new IllegalStateException("Must provide properties file on classpath: " + propertiesFile);
    }

    return properties;
  }

  Properties(final String name, final java.util.Properties properties) {
    this.name = name;
    this.properties = properties;
  }

  public Boolean getBoolean(final String key, final Boolean defaultValue) {
    String value = getString(key, defaultValue.toString());
    return Boolean.parseBoolean(value);
  }

  public Float getFloat(final String key, final Float defaultValue) {
    String value = getString(key, defaultValue.toString());
    return Float.parseFloat(value);
  }

  public Integer getInteger(final String key, final Integer defaultValue) {
    String value = getString(key, defaultValue.toString());
    return Integer.parseInt(value);
  }

  public String getString(final String key, final String defaultValue) {
    return properties.getProperty(key(key), defaultValue);
  }

  private String key(final String key) {
    return "resource." + name + "." + key;
  }
}
