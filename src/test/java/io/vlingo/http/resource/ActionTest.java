// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Test;

import io.vlingo.http.Method;
import io.vlingo.http.QueryParameters;
import io.vlingo.http.resource.Action.MatchResults;

public class ActionTest {

  @Test
  public void testMatchesNoParameters() throws Exception {
    final Action action = new Action(0, "GET", "/users", "queryUsers()", null, false);
    
    final MatchResults matchResults = action.matchWith(Method.GET, new URI("/users"));
    
    assertTrue(matchResults.isMatched());
    assertEquals(0, matchResults.parameterCount());
    assertSame(action, matchResults.action);
  }

  @Test
  public void testMatchesOneParameterInBetween() throws Exception {
    final Action action = new Action(0, "PATCH", "/users/{userId}/name", "changeName(String userId)", null, false);
    
    final MatchResults matchResults = action.matchWith(Method.PATCH, new URI("/users/1234567/name"));
    
    assertTrue(matchResults.isMatched());
    assertSame(action, matchResults.action);
    assertEquals(1, matchResults.parameterCount());
    assertEquals("userId", matchResults.parameters().get(0).name);
    assertEquals("1234567", matchResults.parameters().get(0).value);
  }

  @Test
  public void testMatchesOneParameterLastPosition() throws Exception {
    final Action action = new Action(0, "GET", "/users/{userId}", "queryUser(String userId)", null, false);
    
    final MatchResults matchResults = action.matchWith(Method.GET, new URI("/users/1234567"));
    
    assertTrue(matchResults.isMatched());
    assertSame(action, matchResults.action);
    assertEquals(1, matchResults.parameterCount());
    assertEquals("userId", matchResults.parameters().get(0).name);
    assertEquals("1234567", matchResults.parameters().get(0).value);
  }

  @Test
  public void testMatchesMultipleParameters() throws Exception {
    final Action action =
            new Action(
                    0,
                    "GET",
                    "/catalogs/{catalogId}/products/{productId}/details/{detailId}",
                    "queryCatalogProductDetails(String catalogId, String productId, String detailId)",
                    null,
                    false);
    
    final MatchResults matchResults = action.matchWith(Method.GET, new URI("/catalogs/123/products/4567/details/890"));
    
    assertTrue(matchResults.isMatched());
    assertSame(action, matchResults.action);
    assertEquals(3, matchResults.parameterCount());
    assertEquals("catalogId", matchResults.parameters().get(0).name);
    assertEquals("123", matchResults.parameters().get(0).value);
    assertEquals("productId", matchResults.parameters().get(1).name);
    assertEquals("4567", matchResults.parameters().get(1).value);
    assertEquals("detailId", matchResults.parameters().get(2).name);
    assertEquals("890", matchResults.parameters().get(2).value);
  }

  @Test
  public void testMatchesOneParameterWithEndSlash() throws Exception {
    final Action action = new Action(0, "GET", "/users/{userId}/", "queryUser(String userId)", null, false);
    
    final MatchResults matchResults = action.matchWith(Method.GET, new URI("/users/1234567/"));
    
    assertTrue(matchResults.isMatched());
    assertSame(action, matchResults.action);
    assertEquals(1, matchResults.parameterCount());
    assertEquals("userId", matchResults.parameters().get(0).name);
    assertEquals("1234567", matchResults.parameters().get(0).value);
  }

  @Test
  public void testMatchesMultipleParametersWithEndSlash() throws Exception {
    final Action action =
            new Action(
                    0,
                    "GET",
                    "/users/{userId}/emailAddresses/{emailAddressId}/",
                    "queryUserEmailAddress(String userId, String emailAddressId)",
                    null,
                    false);
    
    final MatchResults matchResults = action.matchWith(Method.GET, new URI("/users/1234567/emailAddresses/890/"));
    
    assertTrue(matchResults.isMatched());
    assertSame(action, matchResults.action);
    assertEquals(2, matchResults.parameterCount());
    assertEquals("userId", matchResults.parameters().get(0).name);
    assertEquals("1234567", matchResults.parameters().get(0).value);
    assertEquals("emailAddressId", matchResults.parameters().get(1).name);
    assertEquals("890", matchResults.parameters().get(1).value);
  }

  @Test
  public void testNoMatchMethod() throws Exception {
    final Action action = new Action(0, "GET", "/users/all", "queryUsers()", null, false);
    
    final MatchResults matchResults = action.matchWith(Method.POST, new URI("/users"));
    
    assertFalse(matchResults.isMatched());
    assertNull(matchResults.action);
    assertEquals(0, matchResults.parameterCount());
  }

  @Test
  public void testNoMatchNoParameters() throws Exception {
    final Action action = new Action(0, "GET", "/users/all", "queryUsers()", null, false);
    
    final MatchResults matchResults = action.matchWith(Method.GET, new URI("/users/one"));
    
    assertFalse(matchResults.isMatched());
    assertNull(matchResults.action);
    assertEquals(0, matchResults.parameterCount());
  }

  @Test
  public void testNoMatchGivenAdditionalElements() throws Exception {
    final Action action = new Action(0, "GET", "/users/{id}", "queryUsers(String userId)", null, false);

    final MatchResults matchResults = action.matchWith(Method.GET, new URI("/users/1234/extra"));

    assertFalse(matchResults.isMatched());
    assertNull(matchResults.action);
    assertEquals(0, matchResults.parameterCount());
  }

  @Test
  public void testNoMatchEmptyParam() throws Exception {
    final Action action = new Action(0, "GET", "/users/{id}/data", "queryUserData(String userId)", null, true);

    final MatchResults matchResults = action.matchWith(Method.GET, new URI("/users//data"));

    assertFalse(matchResults.isMatched());
    assertSame(action, matchResults.action);
    assertEquals(0, matchResults.parameterCount());
  }

  @Test
  public void testMatchEmptyParamGivenAllowsTrailingSlash() throws Exception {
    final Action action = new Action(0, "GET", "/users/{id}", "queryUsers(String userId)", null, false);

    final MatchResults matchResults = action.matchWith(Method.GET, new URI("/users//"));

    assertTrue(matchResults.isMatched());
    assertSame(action, matchResults.action);
    assertEquals(1, matchResults.parameterCount());
  }


  @Test
  public void testNoMatchMultipleParametersMissingSlash() throws Exception {
    final Action action =
            new Action(
                    0,
                    "GET",
                    "/users/{userId}/emailAddresses/{emailAddressId}/",
                    "queryUserEmailAddress(String userId, String emailAddressId)",
                    null,
                    false);
    
    final MatchResults matchResults = action.matchWith(Method.GET, new URI("/users/1234567/emailAddresses/890"));
    
    assertFalse(matchResults.isMatched());
    assertNull(matchResults.action);
    assertEquals(0, matchResults.parameterCount());
  }

  @Test
  public void testWeirdMatchMultipleParametersNoSlash() throws Exception {
    final Action action =
            new Action(
                    0,
                    "GET",
                    "/users/{userId}/emailAddresses/{emailAddressId}",
                    "queryUserEmailAddress(String userId, String emailAddressId)",
                    null,
                    false);
    
    final MatchResults matchResults = action.matchWith(Method.GET, new URI("/users/1234567/emailAddresses/890/"));
    
    assertTrue(matchResults.isMatched());
    assertSame(action, matchResults.action);
    assertEquals(2, matchResults.parameterCount());
    assertNotEquals("890", matchResults.parameters().get(1).value);
    assertEquals("890/", matchResults.parameters().get(1).value); // TODO: may watch for last "/" or add optional configuration
  }

  @Test
  public void testWithQueryParameters() throws Exception {
    final Action action =
            new Action(
                    0,
                    "GET",
                    "/users/{userId}",
                    "queryUser(String userId)",
                    null,
                    false);

    final URI uri = new URI("/users/1234567?one=1.1&two=2.0&three=three*&three=3.3");
    final MatchResults matchResults = action.matchWith(Method.GET, uri);
    assertTrue(matchResults.isMatched());
    assertSame(action, matchResults.action);
    assertEquals(1, matchResults.parameterCount());
    assertEquals("1234567", matchResults.parameters().get(0).value);

    final QueryParameters queryParameters = new QueryParameters(uri.getQuery());
    assertEquals(3, queryParameters.names().size());
    assertEquals(1, queryParameters.valuesOf("one").size());
    assertEquals("1.1", queryParameters.valuesOf("one").get(0));
    assertEquals(1, queryParameters.valuesOf("two").size());
    assertEquals("2.0", queryParameters.valuesOf("two").get(0));
    assertEquals(2, queryParameters.valuesOf("three").size());
    assertEquals("three*", queryParameters.valuesOf("three").get(0));
    assertEquals("3.3", queryParameters.valuesOf("three").get(1));
  }
}
