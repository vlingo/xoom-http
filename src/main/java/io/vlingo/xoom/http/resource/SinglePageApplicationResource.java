package io.vlingo.xoom.http.resource;

import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.http.Body;
import io.vlingo.xoom.http.Response;

import javax.activation.MimetypesFileTypeMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;

import static io.vlingo.xoom.http.Header.Headers.of;
import static io.vlingo.xoom.http.RequestHeader.ContentType;
import static io.vlingo.xoom.http.Response.Status.*;
import static io.vlingo.xoom.http.ResponseHeader.ContentLength;
import static io.vlingo.xoom.http.ResponseHeader.of;
import static io.vlingo.xoom.http.resource.ResourceBuilder.get;
import static io.vlingo.xoom.http.resource.ResourceBuilder.resource;

public class SinglePageApplicationResource extends ResourceHandler {

  private final MimetypesFileTypeMap mimeMap = new MimetypesFileTypeMap();
  private final String contextPath;
  private final String rootPath;
  private final String indexPagePath;

  public SinglePageApplicationResource() {
    this("/frontend", "/app");
  }

  public SinglePageApplicationResource(String rootPath) {
    this(rootPath, "/app");
  }

  public SinglePageApplicationResource(final String rootPath, final String contextPath) {
    this.rootPath = rootPath;
    this.indexPagePath = rootPath + "/index.html";
    this.contextPath = contextPath;
  }

  @Override
  public Resource<?> routes() {
    final RequestHandler0.Handler0 serve0 = this::serve;
    final RequestHandler1.Handler1<String> serve1 = this::serve;
    final RequestHandler2.Handler2<String, String> serve2 = this::serve;
    final RequestHandler3.Handler3<String, String, String> serve3 = this::serve;
    final RequestHandler4.Handler4<String, String, String, String> serve4 = this::serve;
    final RequestHandler5.Handler5<String, String, String, String, String> serve5 = this::serve;
    final RequestHandler6.Handler6<String, String, String, String, String, String> serve6 = this::serve;
    final RequestHandler7.Handler7<String, String, String, String, String, String, String> serve7 = this::serve;
    final RequestHandler8.Handler8<String, String, String, String, String, String, String, String> serve8 = this::serve;

    return resource("ui", 10,
        get("/")
            .handle(this::redirectToApp),
        get(contextPath + "/")
            .handle(serve0),
        get(contextPath + "/{file}")
            .param(String.class)
            .handle(serve1),
        get(contextPath + "/{path1}/{file}")
            .param(String.class)
            .param(String.class)
            .handle(serve2),
        get(contextPath + "/{path1}/{path2}/{file}")
            .param(String.class)
            .param(String.class)
            .param(String.class)
            .handle(serve3),
        get(contextPath + "/{path1}/{path2}/{path3}/{file}")
            .param(String.class)
            .param(String.class)
            .param(String.class)
            .param(String.class)
            .handle(serve4),
        get(contextPath + "/{path1}/{path2}/{path3}/{path4}/{file}")
            .param(String.class)
            .param(String.class)
            .param(String.class)
            .param(String.class)
            .param(String.class)
            .handle(serve5),
      get(contextPath + "/{path1}/{path2}/{path3}/{path4}/{path5}/{file}")
            .param(String.class)
            .param(String.class)
            .param(String.class)
            .param(String.class)
            .param(String.class)
            .param(String.class)
            .handle(serve6),
      get(contextPath + "/{path1}/{path2}/{path3}/{path4}/{path5}/{path6}/{file}")
            .param(String.class)
            .param(String.class)
            .param(String.class)
            .param(String.class)
            .param(String.class)
            .param(String.class)
            .param(String.class)
            .handle(serve7),
      get(contextPath + "/{path1}/{path2}/{path3}/{path4}/{path5}/{path6}/{path7}/{file}")
            .param(String.class)
            .param(String.class)
            .param(String.class)
            .param(String.class)
            .param(String.class)
            .param(String.class)
            .param(String.class)
            .param(String.class)
            .handle(serve8)
      );
  }

  private Completes<Response> redirectToApp() {
    return Completes.withSuccess(
      Response.of(
        MovedPermanently,
        of(of(ContentLength, 0), of("Location", contextPath + "/"))
      )
    );
  }

  private Completes<Response> serve(String... pathSegments) {
    String path = Paths.get(rootPath, pathSegments).toString().replace("\\", "/");
    if((path.indexOf("/static") != -1))
      path = rootPath + path.substring(path.indexOf("/static"));
    
    URL res = getClass().getResource(path);

    String contentType = null;
    if (res == null || path.equals(rootPath)) {
      path = indexPagePath;
      res = getClass().getResource(path);
      contentType = "text/html";
    }

    if (res == null){
      return Completes.withFailure(Response.of(NotFound));
    }

    if (contentType == null){
      contentType = guessContentType(path);
    }

    try(InputStream is = res.openStream()) {
      byte[] content = read(is); // TODO: implement caching
      return Completes.withSuccess(
        Response.of(
          Ok,
          of(of(ContentType, contentType), of(ContentLength, content.length)),
          Body.bytesToUTF8(content)
        )
      );
    } catch (Exception e) {
      logger().error("Failed to read UI Resource", e);
      return Completes.withFailure(Response.of(InternalServerError));
    }
  }

  private String guessContentType(final String path) {
    return mimeMap.getContentType(path);
  }

  private static byte[] read(final InputStream is) throws IOException {
    byte[] readBytes;
    byte[] buffer = new byte[4096];

    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      int read;
      while ((read = is.read(buffer)) != -1) {
        baos.write(buffer, 0, read);
      }
      readBytes = baos.toByteArray();
    } finally {
      is.close();
    }
    return readBytes;
  }
}
