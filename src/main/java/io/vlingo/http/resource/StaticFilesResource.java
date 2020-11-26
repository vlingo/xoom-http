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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
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

    try {
      final String contentPath = String.join(" ", contentFilePath(rootPath + uri).split("%20"));
      // logger().debug("contentPath: "+contentPath);
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

  private String contentFilePath(final String path) throws IOException {
    final String fileSystemPath = String.join(" ", path.split("%20"));
    if (isDirectory(fileSystemPath)) {
      return withIndexHtmlAppended(fileSystemPath);
    }
    return path;
  }

  private boolean isDirectory(final String path) throws MalformedURLException {
    boolean isDirectory = false;

    File file = null;
    String resource = path;
    URL res = getClass().getResource(resource);
    res = new URL(String.join(" ", res.toExternalForm().split("%20")));
    InputStream in = null;
    OutputStream out = null;
    if (res.getProtocol().equals("jar")) {
      //jar:file:/C:/.../some.jar!/...
      try {
        in = getClass().getResourceAsStream(resource);
        file = File.createTempFile("tempfile", ".tmp");
        out = new FileOutputStream(file);

        byte[] bytes = new byte[2];
        //read a char: if it's a directory, input.read returns -1
        if (in.read(bytes) == -1) {
          isDirectory = true;
        }
        
      } catch (IOException e) {
          e.printStackTrace();
      } finally {
        if (out != null) {
          try {
            out.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        if (in != null) {
          try {
            in.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        file.delete();
      }
    } else {
        //from IDE, not from a JAR
        file = new File(res.getFile());
        if (file == null || !file.exists()) {
          throw new RuntimeException("Error: File " + file + " not found!");
        }
        isDirectory = file.isDirectory();
    }
    return isDirectory;
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
