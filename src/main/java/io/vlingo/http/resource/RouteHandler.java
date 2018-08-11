package io.vlingo.http.resource;

import io.vlingo.http.Request;

@FunctionalInterface
public interface RouteHandler {

    void handler(final Request request, final ResourceHandler response);
}
