package io.vlingo.http.resource;

import io.vlingo.http.resource.Configuration.Sizing;
import io.vlingo.http.resource.Configuration.Timing;
import org.junit.Test;

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
    assertEquals(10, configuration.timing().probeInterval);
    assertEquals(10, configuration.timing().probeTimeout);
    assertEquals(100, configuration.timing().requestMissingContentTimeout);
  }

  @Test
  public void testThatConfigurationConfirgures() {
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
          .withRequestMissingContentTimeout(200));

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
  }
}
