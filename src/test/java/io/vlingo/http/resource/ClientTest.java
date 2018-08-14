package io.vlingo.http.resource;

import static io.vlingo.http.Method.POST;
import static io.vlingo.http.RequestHeader.contentLength;
import static io.vlingo.http.RequestHeader.host;
import static io.vlingo.http.Response.Status.Created;
import static io.vlingo.http.Response.Status.RequestTimeout;
import static io.vlingo.http.ResponseHeader.Location;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.http.Body;
import io.vlingo.http.Request;
import io.vlingo.http.Response;
import io.vlingo.http.ResponseHeader;
import io.vlingo.http.resource.Client.Configuration;
import io.vlingo.http.resource.Configuration.Sizing;
import io.vlingo.http.resource.Configuration.Timing;
import io.vlingo.http.sample.user.model.User;

public class ClientTest extends ResourceTestFixtures {
  private Client client;
  private int expectedHeaderCount;
  private Response expectedResponse;
  private ResponseHeader location;
  private Server server;
  private AtomicInteger unknownResponseCount = new AtomicInteger(0);

  @Test public void testNada() {}

  @Test
  public void testThatClientRequestsAndReceivesResponse() throws Exception {
    final String user = johnDoeUserSerialized;

    final TestUntil until = TestUntil.happenings(1);

    client = Client.using(Configuration.defaultedExceptFor(world.stage(), new UnknownResponseConsumer(until)));

    client.requestWith(
            Request
              .has(POST)
              .and(URI.create("/users"))
              .and(host("vlingo.io"))
              .and(contentLength(user))
              .and(Body.from(user)))
          .after(response -> expectedResponse = response, 5000, Response.of(RequestTimeout))
          .andThen(response -> expectedHeaderCount = response.headers.size())
          .andThen(response -> location = response.headers.headerOf(Location))
          .atLast(response -> until.completeNow());

    until.completes();

    assertNotNull(expectedResponse);
    assertEquals(Created, expectedResponse.status);
    assertEquals(3, expectedHeaderCount);
    assertNotNull(location);
    assertEquals(0, unknownResponseCount.get());
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();

    User.resetId();

    server = Server.startWith(world.stage(), resources, 8080, new Sizing(10, 100, 10240), new Timing(1, 2, 100));

    Thread.sleep(10); // delay for server startup
  }

  @After
  public void tearDown() {
    if (client != null) client.close();
    
    if (server != null) server.stop();
    
    super.tearDown();
  }

  private class UnknownResponseConsumer implements ResponseConsumer {
    private final TestUntil until;
    
    UnknownResponseConsumer(final TestUntil until) {
      this.until = until;
    }

    @Override
    public void consume(final Response response) {
      unknownResponseCount.incrementAndGet();
      System.out.println("UNKNOWN RESPONSE:\n" + response);
      until.completeNow();
    }
  }
}
