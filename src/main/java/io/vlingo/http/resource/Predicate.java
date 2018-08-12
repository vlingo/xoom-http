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

public class Predicate {
    public final Method method;
    public final String uri;
    public final RouteHandler routeHandler;

    public Predicate(final Method method, final String uri, final RouteHandler routeHandler) {
        this.method = method;
        this.uri = uri;
        this.routeHandler = routeHandler;
    }
}
