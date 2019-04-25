package io.vlingo.http.resource;

import io.vlingo.http.Body;
import io.vlingo.http.Header;
import io.vlingo.http.Header.Headers;
import io.vlingo.http.MediaType;
import io.vlingo.http.Request;
import io.vlingo.http.RequestHeader;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseHeader;
import io.vlingo.http.ResponseMediaTypeSelector;
import io.vlingo.http.Version;

public class ObjectResponse<T> {

  private static final MediaType DEFAULT_MEDIA_TYPE = MediaType.Json();

  private final Version version;
  private final Response.Status status;
  private final Headers<ResponseHeader> headers;
  private T entity;
  private Class<T> clazz;

  private ObjectResponse(final Version version,
                         final Response.Status status,
                         final Header.Headers<ResponseHeader> headers,
                         final T entity,
                         final Class<T> clazz) {
    this.version = version;
    this.status = status;
    this.headers = headers;
    this.entity = entity;
    this.clazz = clazz;
  }

  public static <T> ObjectResponse<T> of(final Version version,
                                         final Response.Status status,
                                         final Header.Headers<ResponseHeader> headers,
                                         T entity,
                                         Class<T> classType) {
    return new ObjectResponse<>(version, status, headers, entity, classType);
  }

  public static <T> ObjectResponse<T> of(final Response.Status status,
                                         final Header.Headers<ResponseHeader> headers,
                                         final T entity,
                                         final Class<T> classType) {
    return new ObjectResponse<T>(Version.Http1_1, status, headers, entity, classType);
  }

  public static <T> ObjectResponse<T> of(final Response.Status status,
                                         final T entity,
                                         final Class<T> classType) {
    return new ObjectResponse<>(Version.Http1_1, status, Header.Headers.empty(), entity, classType);
  }

  public Response fromRequest(Request request, MediaTypeMapper mapper) {
    String acceptedMediaTypes = request.headerValueOr(RequestHeader.Accept, DEFAULT_MEDIA_TYPE.mimeType());
    ResponseMediaTypeSelector responseMediaTypeSelector = new ResponseMediaTypeSelector(acceptedMediaTypes);
    MediaType responseMediaType = responseMediaTypeSelector.selectType(mapper.mappedMediaTypes());
    String bodyContent = mapper.from(entity, responseMediaType, clazz);
    Body body = Body.from(bodyContent);
    //headers.and(ResponseHeader.ContentType, responseMediaType.mimeType());
    headers.add(ResponseHeader.of(ResponseHeader.ContentType, responseMediaType.mimeType()));
    return Response.of(version, status, headers, body);
  }
}
