// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource.sse;

import io.vlingo.xoom.actors.CompletesEventually;
import io.vlingo.xoom.actors.Returns;
import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.http.Context;
import io.vlingo.xoom.http.Request;

public class MockSseStreamResource extends SseStreamResource {
  private final CompletesEventually completes;
  public final MockRequestResponseContext requestResponseContext;
  private Request request;

  public MockSseStreamResource(final World world) {
    super(world);

    this.completes = world.completesFor(Returns.value(Completes.using(world.stage().scheduler())));
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
