// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;

import io.vlingo.xoom.http.Header.Headers;
import io.vlingo.xoom.wire.message.Converters;

/***
 * This implementation lacks a validation of very large requests against a predefined
 * max request size: https://github.com/vlingo/xoom-http/issues/82
 * 
 */
public class ResponseParser {
  private final VirtualStateParser virtualStateParser;

  public static ResponseParser parserFor(final ByteBuffer requestContent) {
    return new ResponseParser(requestContent);
  }

  public static ResponseParser parserForBodyOnly(final ByteBuffer requestContent) {
    return new ResponseParser(requestContent, true);
  }

  public boolean hasCompleted() {
    return virtualStateParser.hasCompleted();
  }

  public Response fullResponse() {
    return virtualStateParser.fullResponse();
  }

  public boolean hasFullResponse() {
    return virtualStateParser.hasFullResponse();
  }

  public boolean hasMissingContentTimeExpired(final long timeLimit) {
    return virtualStateParser.hasMissingContentTimeExpired(timeLimit);
  }

  public boolean isKeepAliveConnection() {
    return virtualStateParser.isKeepAliveConnection();
  }

  public boolean isMissingContent() {
    return virtualStateParser.isMissingContent();
  }

  public boolean isStreamContentType() {
    return virtualStateParser.isStreamContentType();
  }

  public void parseNext(final ByteBuffer responseContent) {
    virtualStateParser.includes(responseContent).parse();
  }

  private ResponseParser(final ByteBuffer responseContent) {
    this.virtualStateParser = new VirtualStateParser().includes(responseContent).parse();
  }

  private ResponseParser(final ByteBuffer responseContent, final boolean bodyOnly) {
    this.virtualStateParser = new VirtualStateParser(bodyOnly).includes(responseContent).parse();
  }


}
