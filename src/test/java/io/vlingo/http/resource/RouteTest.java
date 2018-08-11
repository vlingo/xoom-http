package io.vlingo.http.resource;

import io.vlingo.http.Response;
import org.junit.Test;

import static io.vlingo.common.serialization.JsonSerialization.serialized;
import static io.vlingo.http.Method.GET;
import static io.vlingo.http.Response.Ok;
import static io.vlingo.http.resource.ResourceBuilder.define;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RouteTest extends ResourceTestFixtures {

    @Test
    public void setting() {
        final DSLResource userResource = define("user")
                .withHandlerPoolSize(10)
                .route(GET, "/user", (request, response) -> {
                    response.completes().with(Response.of(Ok, serialized("OK")));
                })
                .build();

        assertNotNull(resource);
        assertEquals("user", userResource.name);
        assertEquals(10, userResource.handlerPoolSize);
        assertEquals(1, userResource.handlers.size());
    }
}
