package io.vlingo.http.content_handling;

import io.vlingo.http.ResponseMediaTypeSelector;
import io.vlingo.http.MediaType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResponseMediaTypeSelectorTest {

  @Test
  public void single_media_type_matches() {
    final String  specificTypeAccepted = "application/json";
    ResponseMediaTypeSelector selector = new ResponseMediaTypeSelector(specificTypeAccepted);
    MediaType selected = selector.selectType(new MediaType[]{MediaType.Json()});
    assertEquals(MediaType.Json(), selected);
  }

  @Test
  public void wild_card_media_type_matches() {
    final String  xmlAndJsonSuperTypeAccepted = "application/*";
    ResponseMediaTypeSelector selector = new ResponseMediaTypeSelector(xmlAndJsonSuperTypeAccepted);
    MediaType selected = selector.selectType(new MediaType[]{MediaType.Json()});
    assertEquals(MediaType.Json(), selected);
  }

  @Test
  public void generic_media_type_select_by_order_of_media_type() {
    final String  xmlAndJsonSuperTypeAccepted = "application/*";
    ResponseMediaTypeSelector selector = new ResponseMediaTypeSelector(xmlAndJsonSuperTypeAccepted);
    MediaType selected = selector.selectType(new MediaType[]{MediaType.Xml(), MediaType.Json()});
    assertEquals(MediaType.Xml(), selected);
  }

  @Test
  public void specific_media_type_select_highest_ranked() {
    final String  jsonHigherPriorityXmlLowerPriorityAccepted = "application/xml;q=0.8, application/json";
    ResponseMediaTypeSelector selector = new ResponseMediaTypeSelector(jsonHigherPriorityXmlLowerPriorityAccepted);
    MediaType selected = selector.selectType(new MediaType[]{MediaType.Xml(), MediaType.Json()});
    assertEquals(MediaType.Json(), selected);
  }
}
