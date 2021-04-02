// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
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
import java.util.Random;

import org.junit.Test;

import io.vlingo.http.resource.ResourceTestFixtures;
import io.vlingo.wire.message.Converters;

public class RequestParserTest extends ResourceTestFixtures {
  private List<String> uniqueBodies = new ArrayList<>();

  @Test
  public void testThatSingleRequestParses() {
    final RequestParser parser = RequestParser.parserFor(toByteBuffer(postJohnDoeUserMessage));

    assertTrue(parser.hasCompleted());
    assertTrue(parser.hasFullRequest());
    assertFalse(parser.isMissingContent());
    assertFalse(parser.hasMissingContentTimeExpired(System.currentTimeMillis() + 100));

    final Request request = parser.fullRequest();

    assertNotNull(request);
    assertTrue(request.method.isPOST());
    assertEquals("/users", request.uri.getPath());
    assertTrue(request.version.isHttp1_1());
    assertEquals(johnDoeUserSerialized, request.body.content());
  }

  @Test
  public void testThatTenRequestsParse() {
    final RequestParser parser = RequestParser.parserFor(toByteBuffer(multipleRequestBuilder(10)));

    assertTrue(parser.hasCompleted());
    assertTrue(parser.hasFullRequest());
    assertFalse(parser.isMissingContent());
    assertFalse(parser.hasMissingContentTimeExpired(System.currentTimeMillis() + 100));

    int count = 0;
    final Iterator<String> bodyIterator = uniqueBodies.iterator();
    while (parser.hasFullRequest()) {
      ++count;
      final Request request = parser.fullRequest();

      assertNotNull(request);
      assertTrue(request.method.isPOST());
      assertEquals("/users", request.uri.getPath());
      assertTrue(request.version.isHttp1_1());
      assertTrue(bodyIterator.hasNext());
      final String body = bodyIterator.next();
      assertEquals(body, request.body.content());
    }

    assertEquals(10, count);
  }

  @Test
  public void testThatTwoHundredRequestsParse() {
    final RequestParser parser = RequestParser.parserFor(toByteBuffer(multipleRequestBuilder(200)));

    assertTrue(parser.hasCompleted());
    assertTrue(parser.hasFullRequest());
    assertFalse(parser.isMissingContent());
    assertFalse(parser.hasMissingContentTimeExpired(System.currentTimeMillis() + 100));

    int count = 0;
    final Iterator<String> bodyIterator = uniqueBodies.iterator();
    while (parser.hasFullRequest()) {
      ++count;
      final Request request = parser.fullRequest();

      assertNotNull(request);
      assertTrue(request.method.isPOST());
      assertEquals("/users", request.uri.getPath());
      assertTrue(request.version.isHttp1_1());
      assertTrue(bodyIterator.hasNext());
      final String body = bodyIterator.next();
      assertEquals(body, request.body.content());
    }

    assertEquals(200, count);
  }

  @Test
  public void testThatTwoHundredRequestsParseNextSucceeds() {
    final String manyRequests = multipleRequestBuilder(200);

    final int totalLength = manyRequests.length();

    final Random random = new Random();

    int alteringEndIndex = 1024;
    final RequestParser parser = RequestParser.parserFor(toByteBuffer(manyRequests.substring(0, alteringEndIndex)));
    int startingIndex = alteringEndIndex;

    while (startingIndex < totalLength) {
      final int randomLength = random.nextInt(512) + 1;
      System.out.println("RANDOM: " + randomLength);
      alteringEndIndex = startingIndex + randomLength + (int)(System.currentTimeMillis() % startingIndex);
      if (alteringEndIndex > totalLength) {
        alteringEndIndex = totalLength;
      }
      System.out.println("START: " + startingIndex + " RANDOM: " + randomLength + " END: " + alteringEndIndex);
      parser.parseNext(toByteBuffer(manyRequests.substring(startingIndex, alteringEndIndex)));
      startingIndex = alteringEndIndex;
    }

    assertTrue(parser.hasCompleted());
    assertTrue(parser.hasFullRequest());
    assertFalse(parser.isMissingContent());
    assertFalse(parser.hasMissingContentTimeExpired(System.currentTimeMillis() + 100));

    int count = 0;
    final Iterator<String> bodyIterator = uniqueBodies.iterator();
    while (parser.hasFullRequest()) {
      ++count;
      final Request request = parser.fullRequest();

      assertNotNull(request);
      assertTrue(request.method.isPOST());
      assertEquals("/users", request.uri.getPath());
      assertTrue(request.version.isHttp1_1());
      assertTrue(bodyIterator.hasNext());
      final String body = bodyIterator.next();
      assertEquals(body, request.body.content());
    }

    assertEquals(200, count);
  }

  private static final String asciiWithExtendedCharacters = "ABC: æøåé";
  private static final byte[] asciiWithExtendedCharactersSerialized = Converters.textToBytes(asciiWithExtendedCharacters);
  private static final String postWithHeader =
          "POST /users HTTP/1.1\nHost: vlingo.io\nContent-Length: " + asciiWithExtendedCharactersSerialized.length + "\n\n";
  private static final String postWithExtendedCharacters = postWithHeader + asciiWithExtendedCharacters;

  @Test
  public void testThatExtendedCharacterSetParses() {
    final RequestParser parser = RequestParser.parserFor(toByteBuffer(postWithExtendedCharacters));

    assertTrue(parser.hasFullRequest());
    assertTrue(parser.hasCompleted());
    assertEquals(postWithExtendedCharacters, parser.fullRequest().toString());
  }

  @Test
  public void testThatShortRequestWithExtendedCharacterSetParses() {
    final int postFullLength = postWithExtendedCharacters.length();
    final String shortRequest = postWithExtendedCharacters.substring(0, postFullLength - 2);
    final String remainingRequest = postWithExtendedCharacters.substring(postFullLength - 2);

    final RequestParser parser = RequestParser.parserFor(toByteBuffer(shortRequest));

    parser.parseNext(toByteBuffer(remainingRequest));

    assertTrue(parser.hasFullRequest());
    assertTrue(parser.hasCompleted());
    assertEquals(postWithExtendedCharacters, parser.fullRequest().toString());
  }

  private String multipleRequestBuilder(final int amount) {
    final StringBuilder builder = new StringBuilder();

    for (int idx = 1; idx <= amount; ++idx) {
      final String body = (idx % 2 == 0) ? uniqueJaneDoe() : uniqueJohnDoe();
      uniqueBodies.add(body);
      builder.append(postRequest(body));
    }

    return builder.toString();
  }
}
