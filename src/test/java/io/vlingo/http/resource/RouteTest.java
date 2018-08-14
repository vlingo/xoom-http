/*
 * Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the
 * Mozilla Public License, v. 2.0. If a copy of the MPL
 * was not distributed with this file, You can obtain
 * one at https://mozilla.org/MPL/2.0/.
 */

package io.vlingo.http.resource;

import io.vlingo.http.Response;
import org.junit.Test;

import static io.vlingo.common.serialization.JsonSerialization.serialized;
import static io.vlingo.http.Response.Status.Ok;
import static io.vlingo.http.resource.ResourceBuilder.route;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RouteTest {

    @Test
    public void setting() {
        final DynamicResource userResource = route("user")
                .withHandlerPoolSize(10)
                .get("/user", request -> Response.of(Ok, serialized("OK")))
                .build();

        assertNotNull(userResource);
        assertEquals("user", userResource.name);
        assertEquals(10, userResource.handlerPoolSize);
        assertEquals(1, userResource.handlers.size());
    }
}
