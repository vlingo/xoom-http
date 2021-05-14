package io.vlingo.xoom.http.resource;

import io.vlingo.xoom.common.Tuple2;
import io.vlingo.xoom.http.*;
import io.vlingo.xoom.http.Header.Headers;
import io.vlingo.xoom.http.resource.Configuration.Sizing;
import io.vlingo.xoom.http.resource.Configuration.Timing;
import org.junit.Test;

import java.net.URI;

import static io.vlingo.xoom.http.Filters.noResponseFilters;
import static io.vlingo.xoom.http.Method.POST;
import static io.vlingo.xoom.http.Response.Status.Ok;
import static io.vlingo.xoom.http.Version.Http1_1;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConfigurationTest {

  @Test
  public void testThatConfigurationDefaults() {
    final Configuration configuration =
      Configuration.define();

    assertNotNull(configuration);
    assertEquals(8080, configuration.port());

    assertNotNull(configuration.sizing());
    assertEquals(10, configuration.sizing().dispatcherPoolSize);
    assertEquals(100, configuration.sizing().maxBufferPoolSize);
    assertEquals(65535, configuration.sizing().maxMessageSize);

    assertNotNull(configuration.timing());
    assertEquals(4, configuration.timing().probeInterval);
    assertEquals(100, configuration.timing().requestMissingContentTimeout);
  }

  @Test
  public void testThatConfigurationConfigures() {
    final Configuration configuration =
      Configuration.define()
        .withPort(9000)
        .with(Sizing.define()
          .withDispatcherPoolSize(20)
          .withMaxBufferPoolSize(200)
          .withMaxMessageSize(3333))
        .with(Timing.define()
          .withProbeInterval(30)
          .withProbeTimeout(40)
          .withRequestMissingContentTimeout(200))
        .with(Filters.are(singletonList(DUMMY_FILTER),
          noResponseFilters()));

    assertNotNull(configuration);
    assertEquals(9000, configuration.port());

    assertNotNull(configuration.sizing());
    assertEquals(20, configuration.sizing().dispatcherPoolSize);
    assertEquals(200, configuration.sizing().maxBufferPoolSize);
    assertEquals(3333, configuration.sizing().maxMessageSize);

    assertNotNull(configuration.timing());
    assertEquals(30, configuration.timing().probeInterval);
    assertEquals(40, configuration.timing().probeTimeout);
    assertEquals(200, configuration.timing().requestMissingContentTimeout);

    assertNotNull(configuration.filters());
    assertEquals(POST, configuration.filters().process(REQUEST).method);
    assertEquals(Ok, configuration.filters().process(RESPONSE).status);
  }

  private static final Request REQUEST = Request.from(Method.GET, URI.create("/"), Http1_1, Headers.empty(), Body.empty());

  private static final Response RESPONSE = Response.of(Ok, Body.empty());

  private static final RequestFilter DUMMY_FILTER = new RequestFilter() {
    @Override
    public Tuple2<Request, Boolean> filter(final Request request) {
      return Tuple2.from(Request.from(POST, URI.create("/"), Http1_1, Headers.empty(), Body.Empty), false);
    }

    @Override
    public void stop() {
    }
  };
}
