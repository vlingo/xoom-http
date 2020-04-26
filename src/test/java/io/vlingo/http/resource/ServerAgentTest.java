// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

public class ServerAgentTest extends ServerTest {

  @Override
  protected Server startServer() {
    return Server.startWithAgent(world.stage(), resources, serverPort, 100);
  }
}
