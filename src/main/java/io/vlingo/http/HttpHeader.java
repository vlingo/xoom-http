// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

public abstract class HttpHeader {
  public final String name;
  public final String value;

  protected HttpHeader(final String name, final String value) {
    this.name = name;
    this.value = value;
  }
}
