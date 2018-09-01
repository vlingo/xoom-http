/*
 * Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.resource;

import io.vlingo.http.Request;

import java.util.function.BiFunction;

interface PathParameterResolver  {

  static <T> BiFunction<Request, Action.MappedParameters, T> resolvePathParameter(int index, Class<T> paramClass) {
    return (request, mappedParameters) -> {
      Object value = mappedParameters.mapped.get(index).value;
      if (paramClass.isInstance(value)) {
        return (T) value;
      }
      throw new IllegalArgumentException("Value " + value + " is of type " + mappedParameters.mapped.get(0).type + " instead of " + paramClass.getSimpleName());
    };
  }
}
