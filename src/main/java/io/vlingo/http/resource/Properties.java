// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

public class Properties {
  static final java.util.Properties properties;
  private static final String propertiesFile = "/vlingo-http.properties";

  static {
    properties = loadProperties(); 
  }

  static java.util.Properties loadProperties() {
    final java.util.Properties properties = new java.util.Properties();

    try {
      properties.load(Properties.class.getResourceAsStream(propertiesFile));
    } catch (Exception e) {
      throw new IllegalStateException("Must provide properties file on classpath: " + propertiesFile);
    }

    return properties;
  }
}
