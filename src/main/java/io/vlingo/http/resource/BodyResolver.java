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

interface BodyResolver {

  static <T> BiFunction<Request, Action.MappedParameters, T> resolveBody(Class<T> paramClass) {
    return resolveBody(paramClass, DefaultMapper.instance);
  }

  static <T> BiFunction<Request, Action.MappedParameters, T> resolveBody(Class<T> paramClass, Mapper mapper) {
    return (request, mappedParameters) -> mapper.from(request.body.toString(), paramClass);
  }
}
