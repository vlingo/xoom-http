// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http.resource;

import static io.vlingo.xoom.http.Response.Status.InternalServerError;
import static io.vlingo.xoom.http.Response.Status.NotFound;
import static io.vlingo.xoom.http.Response.Status.Ok;
import static io.vlingo.xoom.http.ResponseHeader.ContentLength;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.IOUtils;

import io.vlingo.xoom.http.Body;
import io.vlingo.xoom.http.Header;
import io.vlingo.xoom.http.RequestHeader;
import io.vlingo.xoom.http.Response;
import io.vlingo.xoom.http.ResponseHeader;

/**
 * Serves static file resources. Note that the current limit of 2GB file sizes.
 */
public class StaticFilesResource extends ResourceHandler {
  private String rootPath;

  /**
   * Constructs my default state.
   */
  public StaticFilesResource() {
  }

  /**
   * Completes with {@code Ok} and the file content or {@code NotFound}.
   *
   * @param contentFile   the String name of the content file to be served
   * @param root          the String root path of the static content
   * @param validSubPaths the String indicating the valid file paths under the root
   */
  public void serveFile(final String contentFile, final String root, final String validSubPaths) {
    if (rootPath == null) {
      final String initialSlash = root.startsWith("/") ? "" : "/";
      rootPath = initialSlash + (root.endsWith("/") ? root.substring(0, root.length() - 1) : root);
    }

    final String uri = contentFile.isEmpty() ? "/index.html" : context.request.uri.toString();

    Response response = Arrays.asList(
      rootPath + uri,
      withIndexHtmlAppended(rootPath + uri)
    ).stream()
      .map(this::cleanPath)
      .filter(this::isFile)
      .findFirst()
      .map(this::fileResponse)
      .orElseGet(this::notFound);

    completes().with(response);
  }

  private String cleanPath(String path) {
    return String.join(" ", path.split("%20"));
  }

  private boolean isFile(String path) {
    try {
      URL res = StaticFilesResource.class.getResource(path);
      if (res == null) {
        return false;
      }

      if (!Arrays.asList("jar", "resource").contains(res.getProtocol()))
        return new File(res.toURI()).isFile();

      //jar:file:/C:/.../some.jar!/...
      try (InputStream in = getClass().getResourceAsStream(path)) {
        byte[] bytes = new byte[2];
        //read a char: if it's a directory, input.read returns -1
        if (in.read(bytes) == -1) {
          return false;
        }
      }

      return true;

    } catch (Throwable e) {
      return false;
    }
  }

  private String withIndexHtmlAppended(final String path) {
    final StringBuilder builder = new StringBuilder(path);

    if (!path.endsWith("/")) {
      builder.append("/");
    }

    builder.append("index.html");

    return builder.toString();
  }

  private byte[] readFile(final String path) throws IOException {
    final InputStream contentStream = StaticFilesResource.class.getResourceAsStream(path);
    if (contentStream != null && contentStream.available() > 0) {
      return IOUtils.toByteArray(contentStream);
    }
    throw new IllegalArgumentException("File not found.");
  }

  private String guessContentType(final String path) {
    final String contentType =
      new MimetypesFileTypeMap().getContentType(Paths.get(path).toFile());
    return (contentType != null) ? contentType : "application/octet-stream";
  }

  private Response fileResponse(String path) {
    try {
      final byte[] fileContent = readFile(path);
      return Response.of(Ok,
        Header.Headers.of(
          ResponseHeader.of(RequestHeader.ContentType, guessContentType(path)),
          ResponseHeader.of(ContentLength, fileContent.length)),
        Body.from(fileContent, Body.Encoding.UTF8).content());
    } catch (IOException e) {
      return internalServerError(e);
    } catch (IllegalArgumentException e) {
      return notFound();
    }
  }

  private Response internalServerError(Exception e) {
    logger().error("Internal server error because: " + e.getMessage(), e);
    return Response.of(InternalServerError);
  }

  private Response notFound() {
    return Response.of(NotFound);
  }
}
