/*
 * Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.resource;

import io.vlingo.http.Method;

import java.util.Arrays;

public interface ResourceBuilder {

  static Resource<?> resource(final String name, RequestHandler... requestHandlers) {
    return resource(name, 10, requestHandlers);
  }

  static Resource<?> resource(final String name, final int handlerPoolSize, RequestHandler... requestHandlers) {
    return new DynamicResource(name, handlerPoolSize, Arrays.asList(requestHandlers));
  }

  static RequestHandler0 get(final String uri) {
    return new RequestHandler0(Method.GET, uri);
  }

  static RequestHandler0 post(final String uri) {
    return new RequestHandler0(Method.POST, uri);
  }
}
