// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import static io.vlingo.http.Response.Status.InternalServerError;
import static io.vlingo.http.Response.Status.NotFound;
import static io.vlingo.http.Response.Status.Ok;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import io.vlingo.http.Body;
import io.vlingo.http.Response;

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
      final String slash = root.endsWith("/") ? "" : "/";
      rootPath = root + slash;
    }

    final String contentPath = rootPath + context.request.uri;

    try {
      final byte[] fileContent = readFile(contentPath);
      completes().with(Response.of(Ok, Body.from(fileContent, Body.Encoding.UTF8).content));
    } catch (IOException e) {
      completes().with(Response.of(InternalServerError));
    } catch (IllegalArgumentException e) {
      completes().with(Response.of(NotFound));
    }
  }

  private byte[] readFile(final String path) throws IOException {
    final File file = new File(path);
    if (file.exists()) {
      return Files.readAllBytes(file.toPath());
    }
    throw new IllegalArgumentException("File not found.");
  }
}
