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
import io.vlingo.http.Request;
import io.vlingo.http.Response;

import java.lang.reflect.InvocationTargetException;

public interface RequestHandler {

  Response execute(final Request request, final Action.MappedParameters mappedParameters) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException;

  Method method();

  String path();

  String actionSignature();
}
