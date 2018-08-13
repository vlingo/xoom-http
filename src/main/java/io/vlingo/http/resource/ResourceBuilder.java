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

import java.util.ArrayList;
import java.util.List;

public class ResourceBuilder {
    private String name;
    private int handlerPoolSize;
    private final List<Predicate> handlers;

    public static ResourceBuilder route(final String name) {
        return new ResourceBuilder(name);
    }

    public ResourceBuilder get(final String uri, final RouteHandler routeHandler) {
        this.handlers.add(new Predicate(Method.GET, uri, routeHandler));
        return this;
    }

  public ResourceBuilder post(final String uri, final RouteHandler routeHandler) {
    this.handlers.add(new Predicate(Method.POST, uri, routeHandler));
    return this;
  }

    public ResourceBuilder withHandlerPoolSize(final int pool) {
        this.handlerPoolSize = pool;
        return this;
    }

    public DynamicResource build() {
        return new DynamicResource(this.name, this.handlerPoolSize, this.handlers);
    }

    public ResourceBuilder(String name) {
        this.name = name;
        this.handlerPoolSize = 10;
        this.handlers = new ArrayList<>();
    }
}
