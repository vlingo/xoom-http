// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.media;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ResponseContentMediaTypeSelectorTest {

  @Test
  public void single_media_type_matches() {
    final String  specificTypeAccepted = "application/json";
    ResponseMediaTypeSelector selector = new ResponseMediaTypeSelector(specificTypeAccepted);
    ContentMediaType selected = selector.selectType(new ContentMediaType[]{ContentMediaType.Json()});
    assertEquals(ContentMediaType.Json(), selected);
  }

  @Test
  public void wild_card_media_type_matches() {
    final String  xmlAndJsonSuperTypeAccepted = "application/*";
    ResponseMediaTypeSelector selector = new ResponseMediaTypeSelector(xmlAndJsonSuperTypeAccepted);
    ContentMediaType selected = selector.selectType(new ContentMediaType[]{ContentMediaType.Json()});
    assertEquals(ContentMediaType.Json(), selected);
  }

  @Test
  public void generic_media_type_select_by_order_of_media_type() {
    final String  xmlAndJsonSuperTypeAccepted = "application/*";
    ResponseMediaTypeSelector selector = new ResponseMediaTypeSelector(xmlAndJsonSuperTypeAccepted);
    ContentMediaType selected = selector.selectType(new ContentMediaType[]{ContentMediaType.Xml(), ContentMediaType.Json()});
    assertEquals(ContentMediaType.Xml(), selected);
  }

  @Test
  public void specific_media_type_select_highest_ranked() {
    final String  jsonHigherPriorityXmlLowerPriorityAccepted = "application/xml;q=0.8, application/json";
    ResponseMediaTypeSelector selector = new ResponseMediaTypeSelector(jsonHigherPriorityXmlLowerPriorityAccepted);
    ContentMediaType selected = selector.selectType(new ContentMediaType[]{ContentMediaType.Xml(), ContentMediaType.Json()});
    assertEquals(ContentMediaType.Json(), selected);
  }
}
