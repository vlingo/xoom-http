// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import static io.vlingo.xoom.common.serialization.JsonSerialization.serialized;
import static io.vlingo.xoom.http.Response.Status.Created;
import static io.vlingo.xoom.http.Response.Status.NotFound;
import static io.vlingo.xoom.http.Response.Status.Ok;
import static io.vlingo.xoom.http.resource.ResourceBuilder.get;
import static io.vlingo.xoom.http.resource.ResourceBuilder.post;
import static io.vlingo.xoom.http.resource.ResourceBuilder.resource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.http.Response;

public class FluentTestResource extends ResourceHandler {
  private final Map<String,Data> entities;

  public FluentTestResource(final World world) {
    this.entities = new ConcurrentHashMap<>();
  }

  public Completes<Response> defineWith(final Data data) {
    final Data taggedData = new Data(data, Thread.currentThread().getId());

    entities.put(data.id, taggedData);

    return Completes.withSuccess(Response.of(Created, serialized(taggedData)));
  }

  public Completes<Response> queryRes(final String resId) {
    final Data data = entities.get(resId);

    return Completes.withSuccess(data == null ?
            Response.of(NotFound) :
            Response.of(Ok, serialized(data)));
  }

  @Override
  public Resource<?> routes() {
    return resource("Resource", 5,
            post("/res")
              .body(Data.class)
              .handle(this::defineWith),
            get("/res/{resId}")
              .param(String.class)
              .handle(this::queryRes));
  }

  public static class Data {
    private static AtomicInteger nextId = new AtomicInteger(0);

    public final String id;
    public final String name;
    public final String description;
    public final long resourceHandlerId;

    public static Data with(final String name, final String description) {
      return new Data(String.valueOf(nextId.incrementAndGet()), name, description, -1L);
    }

    public Data(final Data data, final long resourceHandlerId) {
      this(data.id, data.name, data.description, resourceHandlerId);
    }

    public Data(final String id, final String name, final String description, final long resourceHandlerId) {
      this.id = id;
      this.name = name;
      this.description = description;
      this.resourceHandlerId = resourceHandlerId;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((description == null) ? 0 : description.hashCode());
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
    }

    @Override
    public boolean equals(final Object other) {
      if (this == other) {
        return true;
      }

      if (other == null || other.getClass() != getClass()) {
        return false;
      }

      final Data otherData = (Data) other;

      return id.equals(otherData.id) && name.equals(otherData.name) && description.equals(otherData.description);
    }
  }
}
