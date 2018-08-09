package io.vlingo.http.resource;

import org.junit.Test;

import static org.junit.Assert.*;

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
}