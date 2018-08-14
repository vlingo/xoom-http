// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

public enum Method {
  POST("POST") {
    @Override public boolean isPOST() { return true; }
  },
  GET("GET") {
    @Override public boolean isGET() { return true; }
  },
  PUT("PUT") {
    @Override public boolean isPUT() { return true; }
  },
  PATCH("PATCH") {
    @Override public boolean isPATCH() { return true; }
  },
  DELETE("DELETE") {
    @Override public boolean isDELETE() { return true; }
  },
  HEAD("HEAD") {
    @Override public boolean isHEAD() { return true; }
  },
  TRACE("TRACE") {
    @Override public boolean isTRACE() { return true; }
  },
  OPTIONS("OPTIONS") {
    @Override public boolean isOPTIONS() { return true; }
  },
  CONNECT("CONNECT") {
    @Override public boolean isCONNECT() { return true; }
  };

  public static Method from(final String methodNameText) {
    switch (methodNameText) {
    case "POST":
      return POST;
    case "GET":
      return GET;
    case "PUT":
      return PUT;
    case "PATCH":
      return PATCH;
    case "DELETE":
      return DELETE;
    case "HEAD":
      return HEAD;
    case "TRACE":
      return TRACE;
    case "OPTIONS":
      return OPTIONS;
    case "CONNECT":
      return CONNECT;
    default:
      throw new IllegalArgumentException(Response.Status.MethodNotAllowed + "\n\n" + methodNameText);
    }
  }
  
  public final String name;

  private Method(final String name) {
    this.name = name;
  }

  public boolean isCONNECT() { return false; }
  public boolean isDELETE() { return false; }
  public boolean isGET() { return false; }
  public boolean isHEAD() { return false; }
  public boolean isOPTIONS() { return false; }
  public boolean isPATCH() { return false; }
  public boolean isPOST() { return false; }
  public boolean isPUT() { return false; }
  public boolean isTRACE() { return false; }
}
