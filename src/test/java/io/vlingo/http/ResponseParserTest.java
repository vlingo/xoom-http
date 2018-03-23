// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import io.vlingo.http.resource.ResourceTestFixtures;

public class ResponseParserTest extends ResourceTestFixtures {
  private List<String> uniqueBodies = new ArrayList<>();

  @Test
  public void testThatSingleResponseParses() {
    final ResponseParser parser = ResponseParser.parserFor(toByteBuffer(johnDoeCreated()));
    
    assertTrue(parser.hasCompleted());
    assertTrue(parser.hasFullResponse());
    assertFalse(parser.isMissingContent());
    assertFalse(parser.hasMissingContentTimeExpired(System.currentTimeMillis()));
    
    final Response response = parser.fullResponse();
    
    assertNotNull(response);
    assertTrue(response.version.isHttp1_1());
    assertEquals(johnDoeUserSerialized, response.entity.content);
  }

  @Test
  public void testThatTenResponsesParse() {
    final ResponseParser parser = ResponseParser.parserFor(toByteBuffer(multipleResponseBuilder(10)));

    assertTrue(parser.hasCompleted());
    assertTrue(parser.hasFullResponse());
    assertFalse(parser.isMissingContent());
    assertFalse(parser.hasMissingContentTimeExpired(System.currentTimeMillis()));

    int count = 0;
    final Iterator<String> bodyIterator = uniqueBodies.iterator();
    while (parser.hasFullResponse()) {
      ++count;
      final Response response = parser.fullResponse();
      
      assertNotNull(response);
      assertTrue(response.version.isHttp1_1());
      assertTrue(bodyIterator.hasNext());
      final String body = bodyIterator.next();
      assertEquals(body, response.entity.content);
    }

    assertEquals(10, count);
  }

  @Test
  public void testThatTwoHundredResponsesParse() {
    final ResponseParser parser = ResponseParser.parserFor(toByteBuffer(multipleResponseBuilder(200)));

    assertTrue(parser.hasCompleted());
    assertTrue(parser.hasFullResponse());
    assertFalse(parser.isMissingContent());
    assertFalse(parser.hasMissingContentTimeExpired(System.currentTimeMillis()));

    int count = 0;
    final Iterator<String> bodyIterator = uniqueBodies.iterator();
    while (parser.hasFullResponse()) {
      ++count;
      final Response response = parser.fullResponse();
      
      assertNotNull(response);
      assertTrue(response.version.isHttp1_1());
      assertTrue(bodyIterator.hasNext());
      final String body = bodyIterator.next();
      assertEquals(body, response.entity.content);
    }

    assertEquals(200, count);
  }

  @Test
  public void testThatTwoHundredResponsesParseParseNextSucceeds() {
    final String manyResponses = multipleResponseBuilder(200);

    final int totalLength = manyResponses.length();
    int alteringEndIndex = 1024;
    final ResponseParser parser = ResponseParser.parserFor(toByteBuffer(manyResponses.substring(0, alteringEndIndex)));
    int startingIndex = alteringEndIndex;

    while (startingIndex < totalLength) {
      alteringEndIndex = startingIndex + 1024 + (int)(System.currentTimeMillis() % startingIndex);
      if (alteringEndIndex > totalLength) {
        alteringEndIndex = totalLength;
      }
      parser.parseNext(toByteBuffer(manyResponses.substring(startingIndex, alteringEndIndex)));
      startingIndex = alteringEndIndex;
    }

    assertTrue(parser.hasCompleted());
    assertTrue(parser.hasFullResponse());
    assertFalse(parser.isMissingContent());
    assertFalse(parser.hasMissingContentTimeExpired(System.currentTimeMillis()));

    int count = 0;
    final Iterator<String> bodyIterator = uniqueBodies.iterator();
    while (parser.hasFullResponse()) {
      ++count;
      final Response response = parser.fullResponse();
      
      assertNotNull(response);
      assertTrue(response.version.isHttp1_1());
      assertTrue(bodyIterator.hasNext());
      final String body = bodyIterator.next();
      assertEquals(body, response.entity.content);
    }

    assertEquals(200, count);
  }

  private String multipleResponseBuilder(final int amount) {
    final StringBuilder builder = new StringBuilder();
    
    for (int idx = 1; idx <= amount; ++idx) {
      final String body = (idx % 2 == 0) ? uniqueJaneDoe() : uniqueJohnDoe();
      uniqueBodies.add(body);
      builder.append(this.createdResponse(body));
    }

    return builder.toString();
  }
}
