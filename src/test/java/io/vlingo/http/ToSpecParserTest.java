// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.http;

import org.junit.Test;

import io.vlingo.http.resource.Action;
/**
 * ToSpecParserTest
 */
public class ToSpecParserTest {

  @Test
  public void testSimpleUnqualifiedNonBodyParameterMapping() {
    new Action(0, "PATCH", "/airports/{airportId}/stringProperty", "changeStringProperty(String value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/integerProperty", "changeIntegerProperty(int value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/integerProperty", "changeIntegerProperty(Integer value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/longProperty", "changeLongProperty(long value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/longProperty", "changeLongProperty(Long value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/booleanProperty", "changeBooleanProperty(boolean value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/booleanProperty", "changeBooleanProperty(Long value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/doubleProperty", "changeDoubleProperty(double value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/doubleProperty", "changeDoubleProperty(Double value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/shortProperty", "changeShortProperty(short value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/shortProperty", "changeShortProperty(Short value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/floatProperty", "changeFloatProperty(float value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/floatProperty", "changeFloatProperty(Float value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/characterProperty", "changeCharacterProperty(char value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/characterProperty", "changeCharacterProperty(Character value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/byteProperty", "changeByteProperty(byte value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/byteProperty", "changeByteProperty(Byte value)", null, true);
  }
  
  @Test(expected=IllegalStateException.class)
  public void testComplexUnqualifiedNonBodyParameterMapping() {
    new Action(0, "PATCH", "/airports/{airportId}/geocode", "name(body:Geocode geocode)", null, true);
  }

  @Test
  public void testSimpleQualifiedBodyParameterMapping() {
    new Action(0, "PATCH", "/airports/{airportId}/stringProperty", "changeStringProperty(body:java.lang.String value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/integerProperty", "changeIntegerProperty(body:java.lang.Integer value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/longProperty", "changeLongProperty(body:java.lang.Long value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/booleanProperty", "changeBooleanProperty(body:java.lang.Long value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/doubleProperty", "changeDoubleProperty(body:java.lang.Double value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/shortProperty", "changeShortProperty(body:java.lang.Short value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/floatProperty", "changeFloatProperty(body:java.lang.Float value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/characterProperty", "changeCharacterProperty(body:java.lang.Character value)", null, true);
    new Action(0, "PATCH", "/airports/{airportId}/byteProperty", "changeByteProperty(body:java.lang.Byte value)", null, true);
  }
  
  @Test
  public void testComplexQualifiedBodyParameterMapping() {
    new Action(0, "PATCH", "/airports/{airportId}/geocode", "name(body:io.vlingo.http.RequestParameterMappingTest$Geocode geocode)", null, true);
  }

  class Geocode {
    public final Double latitude;
    public final Double longitude;
    
    Geocode(Double lat, Double lon) {
      super();
      this.latitude = lat;
      this.longitude = lon;
    }
  }
}
