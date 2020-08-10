// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import java.util.concurrent.atomic.AtomicReference;

import io.vlingo.actors.Address;
import io.vlingo.actors.CompletesEventually;
import io.vlingo.actors.testkit.AccessSafely;
import io.vlingo.http.Response;

public class MockCompletesEventuallyResponse implements CompletesEventually {
  private AccessSafely withCalls = AccessSafely.afterCompleting(0);

  public AtomicReference<Response> response = new AtomicReference<>();

  /**
   * Answer with an AccessSafely which writes nulls to "with" and reads the write count from the "completed".
   * <p>
   * Note: Clients can replace the default lambdas with their own via readingWith/writingWith.
   *
   * @param n Number of times with(outcome) must be called before readFrom(...) will return.
   * @return
   */
  public AccessSafely expectWithTimes(int n) {
    withCalls = AccessSafely.afterCompleting(n)
        .writingWith("with", (Response r) -> response.set(r))
        .readingWith("completed", () -> withCalls.totalWrites())
        .readingWith("response", () -> response.get());
    return withCalls;
  }

  @Override
  public Address address() {
    return null;
  }

  @Override
  public void with(final Object outcome) {
    withCalls.writeUsing("with", outcome);
  }

  @Override
  public String toString() {
    return "MockCompletesEventuallyResponse [response=" + response + ", withCalls=" + withCalls + "]";
  }
}
