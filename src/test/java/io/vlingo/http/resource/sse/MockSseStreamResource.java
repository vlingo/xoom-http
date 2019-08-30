// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.sse;

import io.vlingo.actors.CompletesEventually;
import io.vlingo.actors.Returns;
import io.vlingo.actors.World;
import io.vlingo.common.BasicCompletes;
import io.vlingo.http.Context;
import io.vlingo.http.Request;

public class MockSseStreamResource extends SseStreamResource {
  private final CompletesEventually completes;
  public final MockRequestResponseContext requestResponseContext;
  private Request request;

  public MockSseStreamResource(final World world) {
    super(world);

    this.completes = world.completesFor(Returns.value(new BasicCompletes<>(world.stage().scheduler())));
    this.requestResponseContext = new MockRequestResponseContext(new MockResponseSenderChannel());
  }

  public void nextRequest(final Request request) {
    this.request = request;
  }

  @Override
  protected CompletesEventually completes() {
    return completes;
  }

  @Override
  protected Context context() {
    return new Context(requestResponseContext, request, completes);
  }
}
