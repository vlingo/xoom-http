// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

public enum Version {
  Http1_1 {
    @Override public boolean isHttp1_1() { return true; }
  },
  Http2_0 {
    // TODO: 2.0 is unsupported, but the version recognizer works
    @Override public boolean isHttp2_0() { return true; }
  };

  public static final String HTTP_1_1 = "HTTP/1.1";
  public static final String HTTP_2_0 = "HTTP/2.0";
  
  public static Version from(final String version) {
    if (version.equals(HTTP_1_1)) {
      return Http1_1;
    } else if (version.equals(HTTP_2_0)) {
      return Http2_0;
    }
    throw new IllegalArgumentException("Unsupported HTTP/version: " + version);
  }

  public boolean isHttp1_1() { return false; }
  public boolean isHttp2_0() { return false; }
  
  @Override
  public String toString() {
    if (this.isHttp1_1()) {
      return HTTP_1_1;
    } else if (this.isHttp2_0()) {
      return HTTP_2_0;
    }
    return "HTTP/version unsupported";
  }
}
