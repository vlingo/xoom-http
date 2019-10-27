// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import static io.vlingo.http.resource.ResourceBuilder.get;
import static io.vlingo.http.resource.ResourceBuilder.resource;

import io.vlingo.common.Completes;
import io.vlingo.http.Response;
import io.vlingo.http.Response.Status;

public class FailResource extends ResourceHandler {
  public FailResource() { }

  public Completes<Response> query() {
    System.out.println("QUERY");
    return Completes.withFailure(Response.of(Status.BadRequest));
  }

  @Override
  public Resource<?> routes() {
    return resource("Failure API",
            get("/fail")
            .handle(this::query));
  }
}
