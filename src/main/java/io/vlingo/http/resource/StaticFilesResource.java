// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import static io.vlingo.http.Response.Status.InternalServerError;
import static io.vlingo.http.Response.Status.NotFound;
import static io.vlingo.http.Response.Status.Ok;
import static io.vlingo.http.ResponseHeader.ContentLength;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.IOUtils;

import io.vlingo.http.Body;
import io.vlingo.http.Header;
import io.vlingo.http.RequestHeader;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseHeader;

/**
 * Serves static file resources. Note that the current limit of 2GB file sizes.
 */
public class StaticFilesResource extends ResourceHandler {
  private String rootPath;

  /**
   * Constructs my default state.
   */
  public StaticFilesResource() { }

  /**
   * Completes with {@code Ok} and the file content or {@code NotFound}.
   * @param contentFile the String name of the content file to be served
   * @param root the String root path of the static content
   * @param validSubPaths the String indicating the valid file paths under the root
   */
  public void serveFile(final String contentFile, final String root, final String validSubPaths) {
    if (rootPath == null) {
      final String initialSlash = root.startsWith("/") ? "" : "/";
      rootPath = initialSlash + (root.endsWith("/") ? root.substring(0, root.length() - 1) : root);
    }

    final String uri = contentFile.isEmpty() ? "/index.html" : context.request.uri.toString();

    final String contentPath = contentFilePath(rootPath + uri);

    try {
      final byte[] fileContent = readFile(contentPath);
      completes().with(Response.of(Ok,
          Header.Headers.of(ResponseHeader.of(RequestHeader.ContentType, guessContentType(contentPath)),
              ResponseHeader.of(ContentLength, fileContent.length)),
          Body.from(fileContent, Body.Encoding.UTF8).content()));
    } catch (IOException e) {
      completes().with(Response.of(InternalServerError));
    } catch (IllegalArgumentException e) {
      completes().with(Response.of(NotFound));
    }
  }

  private String contentFilePath(final String path) {
    final String fileSystemPath = String.join(" ", path.split("%20"));
    if (isDirectory(fileSystemPath)) {
      return withIndexHtmlAppended(fileSystemPath);
    }
    return path;
  }

  private boolean isDirectory(final String path) {
    URL url = getClass().getResource(path);
    if(url==null) return false;
    return new File(String.join(" ", url.getPath().split("%20"))).isDirectory();
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
}
