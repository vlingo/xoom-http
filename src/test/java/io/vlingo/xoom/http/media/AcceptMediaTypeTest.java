// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.media;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.vlingo.xoom.http.media.ResponseMediaTypeSelector.AcceptMediaType;

public class AcceptMediaTypeTest {

  @Test
  public void specificMimeTypeGreaterThanGeneric() {
    AcceptMediaType acceptMediaType1 = new AcceptMediaType("application", "json");
    AcceptMediaType acceptMediaType2 = new AcceptMediaType("*", "*");
    assertEquals( 1, acceptMediaType1.compareTo(acceptMediaType2));
    assertEquals( -1, acceptMediaType2.compareTo(acceptMediaType1));
  }

  @Test
  public void specificMimeSubTypeGreaterThanGeneric() {
    AcceptMediaType acceptMediaType1 = new AcceptMediaType("application", "json");
    AcceptMediaType acceptMediaType2 = new AcceptMediaType("application", "*");
    assertEquals( 1, acceptMediaType1.compareTo(acceptMediaType2));
    assertEquals( -1, acceptMediaType2.compareTo(acceptMediaType1));
  }

  @Test
  public void specificParameterGreaterThanGenericWithSameQualityFactor() {
    AcceptMediaType acceptMediaType1 = new MediaTypeDescriptor.Builder<>(AcceptMediaType::new)
      .withMimeType("application").withMimeSubType("xml").withParameter("version", "1.0")
      .build();

    AcceptMediaType acceptMediaType2 = new AcceptMediaType("application", "json");
    assertEquals( 1, acceptMediaType1.compareTo(acceptMediaType2));
    assertEquals( -1, acceptMediaType2.compareTo(acceptMediaType1));
  }

  @Test
  public void qualityFactorTrumpsSpecificity() {
    AcceptMediaType acceptMediaType1 = new MediaTypeDescriptor.Builder<>(AcceptMediaType::new)
      .withMimeType("text").withMimeSubType("*")
      .build();

    AcceptMediaType acceptMediaType2 = new MediaTypeDescriptor.Builder<>(AcceptMediaType::new)
      .withMimeType("text").withMimeSubType("json")
      .withParameter("q", "0.8")
      .build();

    assertEquals( 1, acceptMediaType1.compareTo(acceptMediaType2));
    assertEquals( -1, acceptMediaType2.compareTo(acceptMediaType1));
  }
}
