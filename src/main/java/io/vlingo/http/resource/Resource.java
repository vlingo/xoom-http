package io.vlingo.http.resource;

import io.vlingo.http.Context;

public abstract class Resource<T> {
    public abstract void dispatchToHandlerWith(final Context context, final Action.MappedParameters mappedParameters);
}
