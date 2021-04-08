// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.
package io.vlingo.xoom.http;

import org.junit.Test;

import io.vlingo.xoom.http.resource.Action;
/**
 * ToSpecParserTest
 */
public class ToSpecParserTest {

  @Test
  public void testSimpleUnqualifiedNonBodyParameterMapping() {
    new Action(0, "PATCH", "/airports/{airportId}/stringProperty", "changeStringProperty(String value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/integerProperty", "changeIntegerProperty(int value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/integerProperty", "changeIntegerProperty(Integer value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/longProperty", "changeLongProperty(long value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/longProperty", "changeLongProperty(Long value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/booleanProperty", "changeBooleanProperty(boolean value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/booleanProperty", "changeBooleanProperty(Long value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/doubleProperty", "changeDoubleProperty(double value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/doubleProperty", "changeDoubleProperty(Double value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/shortProperty", "changeShortProperty(short value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/shortProperty", "changeShortProperty(Short value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/floatProperty", "changeFloatProperty(float value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/floatProperty", "changeFloatProperty(Float value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/characterProperty", "changeCharacterProperty(char value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/characterProperty", "changeCharacterProperty(Character value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/byteProperty", "changeByteProperty(byte value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/byteProperty", "changeByteProperty(Byte value)", null);
  }
  
  @Test(expected=IllegalStateException.class)
  public void testComplexUnqualifiedNonBodyParameterMapping() {
    new Action(0, "PATCH", "/airports/{airportId}/geocode", "name(body:Geocode geocode)", null);
  }

  @Test
  public void testSimpleQualifiedBodyParameterMapping() {
    new Action(0, "PATCH", "/airports/{airportId}/stringProperty", "changeStringProperty(body:java.lang.String value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/integerProperty", "changeIntegerProperty(body:java.lang.Integer value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/longProperty", "changeLongProperty(body:java.lang.Long value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/booleanProperty", "changeBooleanProperty(body:java.lang.Long value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/doubleProperty", "changeDoubleProperty(body:java.lang.Double value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/shortProperty", "changeShortProperty(body:java.lang.Short value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/floatProperty", "changeFloatProperty(body:java.lang.Float value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/characterProperty", "changeCharacterProperty(body:java.lang.Character value)", null);
    new Action(0, "PATCH", "/airports/{airportId}/byteProperty", "changeByteProperty(body:java.lang.Byte value)", null);
  }
  
  @Test
  public void testComplexQualifiedBodyParameterMapping() {
    new Action(0, "PATCH", "/airports/{airportId}/geocode", "name(body:io.vlingo.xoom.http.ToSpecParserTest$Geocode geocode)", null);
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
