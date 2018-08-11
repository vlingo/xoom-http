package io.vlingo.http.resource;

import io.vlingo.http.Response;
import org.junit.Test;

import static io.vlingo.common.serialization.JsonSerialization.serialized;
import static io.vlingo.http.Response.Ok;
import static io.vlingo.http.resource.ResourceBuilder.route;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RouteTest {

    @Test
    public void setting() {
        final DynamicResource userResource = route("user")
                .withHandlerPoolSize(10)
                .get("/user", (request, response) -> {
                    response.completes().with(Response.of(Ok, serialized("OK")));
                })
                .build();

        assertNotNull(userResource);
        assertEquals("user", userResource.name);
        assertEquals(10, userResource.handlerPoolSize);
        assertEquals(1, userResource.handlers.size());
    }
}
