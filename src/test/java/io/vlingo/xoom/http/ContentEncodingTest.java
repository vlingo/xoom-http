package io.vlingo.xoom.http;

import org.junit.Test;

import static org.junit.Assert.*;

public class ContentEncodingTest {

  @Test
  public void createEncodingFrom() {
    ContentEncoding results = ContentEncoding.parseFromHeader("gzip, br");
    ContentEncodingMethod[] expectedMethods = new ContentEncodingMethod[] {
      ContentEncodingMethod.GZIP, ContentEncodingMethod.BROTLI
    };
    assertArrayEquals(expectedMethods, results.encodingMethods);
  }

  @Test
  public void createEncodingSkipsUnkownEncoding() {
    ContentEncoding results = ContentEncoding.parseFromHeader("gzip, br, foo");
    ContentEncodingMethod[] expectedMethods = new ContentEncodingMethod[] {
      ContentEncodingMethod.GZIP, ContentEncodingMethod.BROTLI
    };
    assertArrayEquals(expectedMethods, results.encodingMethods);
  }

  @Test
  public void createEncodingEmpty() {
    ContentEncoding results = ContentEncoding.parseFromHeader("");
    ContentEncodingMethod[] expectedMethods = new ContentEncodingMethod[] {};
    assertArrayEquals(expectedMethods, results.encodingMethods);
  }
}
