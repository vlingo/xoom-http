/*
 * Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.resource;

import static org.junit.Assert.assertEquals;

import io.vlingo.actors.Logger;
import io.vlingo.http.media.ContentMediaType;
import io.vlingo.http.Response;

public class RequestHandlerTestBase {

  protected Logger logger;

  RequestHandlerTestBase() {
    this.logger = Logger.testLogger();
  }

  void assertResponsesAreEquals(final Response expected, final Response actual) {
    assertEquals(expected.toString(), actual.toString());
  }

  <T> void assertResolvesAreEquals(final ParameterResolver<T> expected, final ParameterResolver<T> actual) {
    assertEquals(expected.type, actual.type);
    assertEquals(expected.paramClass, actual.paramClass);
  }

  MediaTypeMapper defaultMediaTypeMapperForJson() {

    return new MediaTypeMapper.Builder()
      .addMapperFor(ContentMediaType.Json(), new DefaultJsonMapper())
      .build();
  }
}
