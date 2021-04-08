// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.xoom.http;
/**
 * QueryParametersParserTest tests the query string parsing capabilities
 * of {@link QueryParameters}.
 */

import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class QueryParametersParserTest {
  
  @Test
  public void testParseNullQuery() {
    String query = null;
    QueryParameters qp = new QueryParameters(query);
    assertEquals("names.size", 0, qp.names().size());
  }
  
  @Test
  public void testParseEmptyQuery() {
    String query = "";
    QueryParameters qp = new QueryParameters(query);
    assertEquals("names.size", 0, qp.names().size());
  }

  @Test
  public void testParseSingleSimpleValuedParameterQuery() {
    String query = "color=red";
    QueryParameters qp = new QueryParameters(query);
    assertEquals("names.size", 1, qp.names().size());
    String name = qp.names().iterator().next();
    assertEquals("name", "color", name);
    List<String> values = qp.valuesOf(name);
    assertEquals("values", 1, values.size());
    String value = values.get(0);
    assertEquals("value", "red", value);
  }
  
  @Test
  public void testParseSingleListValuedParameterQuery() {
    String query = "colors=red,blue,green";
    QueryParameters qp = new QueryParameters(query);
    assertEquals("names.size", 1, qp.names().size());
    String name = qp.names().iterator().next();
    assertEquals("name", "colors", name);
    List<String> values = qp.valuesOf(name);
    assertEquals("values", 1, values.size());
    assertEquals("value", "red,blue,green", values.get(0));
  }
  
  @Test
  public void testParseMultiParameterQuery() {
    String query = "color=red&size=medium";
    QueryParameters qp = new QueryParameters(query);
    assertEquals("names.size", 2, qp.names().size());
    Iterator<String> names = qp.names().iterator();
    
    String name0 = names.next();
    assertEquals("name0", "color", name0);
    List<String> values0 = qp.valuesOf(name0);
    assertEquals("values0", 1, values0.size());
    String value0 = values0.get(0);
    assertEquals("values0", "red", value0);
    
    String name1 = names.next();
    assertEquals("name1", "size", name1);
    List<String> values1 = qp.valuesOf(name1);
    assertEquals("values1", 1, values1.size());
    String value1 = values1.get(0);
    assertEquals("values0", "medium", value1);
  }

  @Test
  public void testParseParameterWithMultipleAmpersand() {
    String query = "color=red&&size=medium";
    QueryParameters qp = new QueryParameters(query);
    assertEquals("names.size", 2, qp.names().size());
    Iterator<String> names = qp.names().iterator();

    String name0 = names.next();
    assertEquals("name0", "color", name0);
    List<String> values0 = qp.valuesOf(name0);
    assertEquals("values0", 1, values0.size());
    String value0 = values0.get(0);
    assertEquals("values0", "red", value0);

    String name1 = names.next();
    assertEquals("name1", "size", name1);
    List<String> values1 = qp.valuesOf(name1);
    assertEquals("values1", 1, values1.size());
    String value1 = values1.get(0);
    assertEquals("values0", "medium", value1);
  }

  @Test
  public void testParseParametersWithAParamWithoutValue() {
    String query = "size";
    QueryParameters qp = new QueryParameters(query);
    assertEquals("names.size", 1, qp.names().size());
    Iterator<String> names = qp.names().iterator();

    String name0 = names.next();
    assertEquals("name0", "size", name0);
    List<String> values0 = qp.valuesOf(name0);
    assertEquals("values0", 1, values0.size());
    String value0 = values0.get(0);
    assertNull("values0", value0);
  }

  @Test
  public void testContainsKeyTrueCase() {
    String query = "size";
    QueryParameters qp = new QueryParameters(query);

    assertTrue("key size is present", qp.containsKey("size"));
  }

  @Test
  public void testContainsKeyFalseCase() {
    String query = "size";
    QueryParameters qp = new QueryParameters(query);

    assertFalse("key color is not present", qp.containsKey("color"));
  }
}
