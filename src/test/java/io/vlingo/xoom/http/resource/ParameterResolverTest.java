// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import io.vlingo.xoom.http.*;
import io.vlingo.xoom.http.media.ContentMediaType;
import io.vlingo.xoom.http.sample.user.NameData;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ParameterResolverTest {
  private Request request;
  private Action.MappedParameters mappedParameters;

  @Before
  public void setUp() {
    request = Request.has(Method.POST)
      .and(Version.Http1_1)
      .and(URI.create("/user/my-post?page=10"))
      .and(RequestHeader.from("Host:www.vlingo.io"))
      .and(Body.from("{\"given\":\"John\",\"family\":\"Doe\"}"));

    mappedParameters = new Action.MappedParameters(1, Method.GET, "ignored", Collections.singletonList(
        new Action.MappedParameter("String", "my-post"))
      );
  }

  @Test
  public void path() {
    final ParameterResolver<String> resolver = ParameterResolver.path(0, String.class);

    String result = resolver.apply(request, mappedParameters);

    assertEquals("my-post", result);
    assertEquals(ParameterResolver.Type.PATH, resolver.type);
  }

  @Test
  public void bodyContentRequest() {
    final byte[] content = new byte[]{0xD, 0xE, 0xA, 0xD, 0xB, 0xE, 0xE, 0xF};
    final String binaryMediaTypeDescriptor = "application/octet-stream";

    final ContentMediaType binaryMediaType = ContentMediaType.parseFromDescriptor(binaryMediaTypeDescriptor);
    Request binaryRequest = Request.has(Method.POST)
      .and(Version.Http1_1)
      .and(URI.create("/user/my-post"))
      .and(RequestHeader.from("Host:www.vlingo.io"))
      .and(RequestHeader.contentType(binaryMediaTypeDescriptor))
      .and(RequestHeader.contentEncoding(ContentEncodingMethod.GZIP.descriptor))
      .and(Body.from(content.clone(), Body.Encoding.None));

    final ParameterResolver<RequestData> resolver = ParameterResolver.body(RequestData.class);

    final RequestData result = resolver.apply(binaryRequest, mappedParameters);
    final RequestData expected = new RequestData(
      Body.from(content.clone(), Body.Encoding.None),
      binaryMediaType,
      new ContentEncoding(ContentEncodingMethod.GZIP));

    assertEquals(expected.contentEncoding, result.contentEncoding);
    assertEquals(expected.mediaType, result.mediaType);
    assertArrayEquals(expected.body.binaryContent(), result.body.binaryContent());
    assertEquals(ParameterResolver.Type.BODY, resolver.type);
  }

  @Test
  public void bodyContentFormData() {
    final String content = "--boundary\n" +
      "Content-Disposition: form-data; name=\"field1\"\n\n" +
      "value1\n" + "--boundary\n" +
      "Content-Disposition: form-data; name=\"field2\"; filename=\"example.txt\"\n\n" +
      "value2\n" + "--boundary--";

    final String binaryMediaTypeDescriptor = "multipart/form-data;boundary=\"boundary\"";

    Request binaryRequest = Request.has(Method.POST)
      .and(Version.Http1_1)
      .and(URI.create("/user/my-post"))
      .and(RequestHeader.from("Host:www.vlingo.io"))
      .and(RequestHeader.contentType(binaryMediaTypeDescriptor))
      .and(Body.from(content));

    final ParameterResolver<RequestData> resolver = ParameterResolver.body(RequestData.class);

    final RequestData result = resolver.apply(binaryRequest, mappedParameters);
    final RequestData expected = new RequestData(
      Body.from(content),
      ContentMediaType.parseFromDescriptor(binaryMediaTypeDescriptor),
      ContentEncoding.none());

    assertEquals(expected.mediaType, result.mediaType);
    assertEquals(expected.contentEncoding, result.contentEncoding);
    assertEquals(expected.body.content(), result.body.content());
    assertEquals(ParameterResolver.Type.BODY, resolver.type);
  }

  @Test
  public void body() {
    final ParameterResolver<NameData> resolver = ParameterResolver.body(NameData.class);

    final NameData result = resolver.apply(request, mappedParameters);
    final NameData expected = new NameData("John", "Doe");

    assertEquals(expected.toString(), result.toString());
    assertEquals(ParameterResolver.Type.BODY, resolver.type);
  }

  @Test
  public void bodyWithContentTypeMapper() {
    final MediaTypeMapper mediaTypeMapper = new MediaTypeMapper.Builder()
      .addMapperFor(ContentMediaType.Json(), DefaultJsonMapper.instance)
      .build();

    final ParameterResolver<NameData> resolver = ParameterResolver.body(NameData.class, mediaTypeMapper);

    final NameData result = resolver.apply(request, mappedParameters);
    final NameData expected = new NameData("John", "Doe");

    assertEquals(expected.toString(), result.toString());
    assertEquals(ParameterResolver.Type.BODY, resolver.type);
  }

  @Test
  public void header() {
    final ParameterResolver<Header> resolver = ParameterResolver.header("Host");

    final Header result = resolver.apply(request, mappedParameters);

    assertEquals("Host", result.name);
    assertEquals("www.vlingo.io", result.value);
    assertEquals(ParameterResolver.Type.HEADER, resolver.type);
  }

  @Test
  public void query() {
    final ParameterResolver<String> resolver = ParameterResolver.query("page");

    final String result = resolver.apply(request, mappedParameters);

    assertEquals("10", result);
    assertEquals(ParameterResolver.Type.QUERY, resolver.type);
  }

  @Test
  public void queryWithType() {
    final ParameterResolver<Integer> resolver = ParameterResolver.query("page", Integer.class);

    final Integer result = resolver.apply(request, mappedParameters);

    assertEquals(10, (int) result);
    assertEquals(ParameterResolver.Type.QUERY, resolver.type);
  }

  @Test
  public void queryShouldReturnDefaultWhenItIsNotPresent() {
    final ParameterResolver<Integer> resolver = ParameterResolver.query("pageSize", Integer.class, 50);

    final Integer result = resolver.apply(request, mappedParameters);

    assertEquals(50, (int) result);
    assertEquals(ParameterResolver.Type.QUERY, resolver.type);
  }
}
