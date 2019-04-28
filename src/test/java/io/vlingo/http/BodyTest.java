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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Base64;

import org.junit.Test;

public class BodyTest {
  private final String BinaryTextBodyText = "This is some text to render as bytes encoded in Base64. Phew!";

  @Test
  public void testThatBodyHasContent() {
    final String content = "{ text:\\\"some text\\\"}\"";

    final Body body = Body.from(content);

    assertNotNull(body);
    assertNotNull(body.content);
    assertTrue(body.hasContent());
    assertEquals(content, body.content);
  }

  @Test
  public void testThatBodyHasNoContent() {
    final Body body = Body.empty();

    assertNotNull(body);
    assertNotNull(body.content);
    assertFalse(body.hasContent());
    assertEquals("", body.content);
  }

  @Test
  public void testThatByteArrayBodyEncodesAsBase64() {
    final byte[] bodyBytes = BinaryTextBodyText.getBytes();
    final Body body = Body.from(bodyBytes);

    assertNotNull(body);
    assertNotNull(body.content);
    assertTrue(body.hasContent());

    final String decoded = new String(Base64.getDecoder().decode(body.content.getBytes()));
    assertEquals(BinaryTextBodyText, decoded);
  }

  @Test
  public void testThatFlippedByteBufferBodyEncodesAsBase64() {
    final ByteBuffer buffer = ByteBuffer.allocate(1000);
    buffer.put(BinaryTextBodyText.getBytes());
    buffer.flip();
    final Body body = Body.from(buffer);

    assertNotNull(body);
    assertNotNull(body.content);
    assertTrue(body.hasContent());

    final String decoded = new String(Base64.getDecoder().decode(body.content.getBytes()));
    assertEquals(BinaryTextBodyText, decoded);
  }

  @Test
  public void testThatNotFlippedByteBufferBodyEncodesAsBase64() {
    final ByteBuffer buffer = ByteBuffer.allocate(1000);
    buffer.put(BinaryTextBodyText.getBytes());
    final Body body = Body.from(buffer);

    assertNotNull(body);
    assertNotNull(body.content);
    assertTrue(body.hasContent());

    final String decoded = new String(Base64.getDecoder().decode(body.content.getBytes()));
    assertEquals(BinaryTextBodyText, decoded);
  }

  @Test
  public void testThatByteArrayBodyEncodesAsUTF8() {
    final byte[] bodyBytes = BinaryTextBodyText.getBytes();
    final Body body = Body.from(bodyBytes, Body.Encoding.UTF8);

    assertNotNull(body);
    assertNotNull(body.content);
    assertTrue(body.hasContent());

    final String decoded = new String(body.content.getBytes(), Charset.forName("UTF-8"));
    assertEquals(BinaryTextBodyText, decoded);
  }

  @Test
  public void testThatFlippedByteBufferBodyEncodesAsUTF8() {
    final ByteBuffer buffer = ByteBuffer.allocate(1000);
    buffer.put(BinaryTextBodyText.getBytes());
    buffer.flip();
    final Body body = Body.from(buffer, Body.Encoding.UTF8);

    assertNotNull(body);
    assertNotNull(body.content);
    assertTrue(body.hasContent());

    final String decoded = new String(body.content.getBytes(), Charset.forName("UTF-8"));
    assertEquals(BinaryTextBodyText, decoded);
  }

  @Test
  public void testThatNotFlippedByteBufferBodyEncodesAsUTF8() {
    final ByteBuffer buffer = ByteBuffer.allocate(1000);
    buffer.put(BinaryTextBodyText.getBytes());
    final Body body = Body.from(buffer, Body.Encoding.UTF8);

    assertNotNull(body);
    assertNotNull(body.content);
    assertTrue(body.hasContent());

    final String decoded = new String(body.content.getBytes(), Charset.forName("UTF-8"));
    assertEquals(BinaryTextBodyText, decoded);
  }

  @Test
  public void testThatBytesEncodeAsExpectedUTF8() {
    final byte[] pdfBytes = { 37, 80, 68, 70, 45, 49, 46, 52, 10, 37, -30, -29, -49, -45, 10, 49 };

    final Body body = Body.from(pdfBytes, Body.Encoding.UTF8);

    assertNotNull(body);
    assertNotNull(body.content);
    assertTrue(body.hasContent());
    assertTrue(body.content.startsWith("%PDF"));
  }
}
