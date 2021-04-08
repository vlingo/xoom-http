// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http;

class ContentPacket {
  final String content;
  final int utf8ExtraLength;

  ContentPacket(final String content, final int utf8ExtraLength) {
    this.content = content;
    this.utf8ExtraLength = utf8ExtraLength;
  }
}
