// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http;

/**
 * Base filter supporting starting and stopping.
 */
public abstract class Filter {
  /**
   * Construct my state.
   */
  protected Filter() { }

  /**
   * Sent when I am to be stopped.
   */
  public abstract void stop();
}
