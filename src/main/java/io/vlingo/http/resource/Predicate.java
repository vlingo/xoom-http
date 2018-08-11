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
