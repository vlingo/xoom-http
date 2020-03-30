// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import io.vlingo.actors.ActorInstantiator;
import io.vlingo.actors.Definition;
import io.vlingo.actors.Stage;
import io.vlingo.actors.Stoppable;
import io.vlingo.common.Completes;
import io.vlingo.http.Filters;
import io.vlingo.http.resource.Configuration.Sizing;
import io.vlingo.http.resource.Configuration.Timing;
import io.vlingo.wire.channel.RefreshableSelector;

/**
 * The protocol of the HTTP {@code Server}, as well as factory methods for starting it.
 * The factory methods will initialize the vlingo-wire {@code RefreshableSelector} as
 * non-refreshing unless you initialize if prior to starting the {@code Server}.
 * <p>
 * NOTE: Override the {@code Server} initialization of RefreshableSelector initialization
 * by using the {@code withCountedThreshold()} count-based refreshing initialization or the
 * {@code withTimedThreshold()} time-based refreshing initialization prior to starting the
 * {@code Server}. This may be done just following the {@code World} start up.
 */
public interface Server extends Stoppable {

  public static Server startWith(final Stage stage) {
    return startWith(stage, Properties.properties);
  }


  /**
   * Answer a new {@code Server} with the given configuration and characteristics.
   * @param stage the Stage in which the Server lives
   * @param properties the java.util.Properties with properties named per vlingo-http.properties
   * @return Server
   */
  public static Server startWith(final Stage stage, java.util.Properties properties) {
    final Configuration configuration = Configuration.defineWith(properties);

    final Resources resources = Loader.loadResources(properties);

    return startWith(
            stage,
            resources,
            configuration.port(),
            configuration.sizing(),
            configuration.timing());
  }

  /**
   * Answer a new {@code Server} with the given configuration and characteristics.
   * @param stage the Stage in which the Server lives
   * @param resources the Resource with URI descriptions that the Server understands
   * @param port the int socket port the Server will run on
   * @param sizing the Sizing such as pool and buffer sizes
   * @param timing the Timing such as probe interval and missing content timeout
   * @return Server
   */
  public static Server startWith(
          final Stage stage,
          final Resources resources,
          final int port,
          final Sizing sizing,
          final Timing timing) {

    return startWith(stage, resources, Filters.none(), port, sizing, timing);
  }

  /**
   * Answer a new {@code Server} with the given configuration and characteristics.
   * @param stage the Stage in which the Server lives
   * @param resources the Resource with URI descriptions that the Server understands
   * @param filters the Filters used to process requests before dispatching to a resource
   * @param port the int socket port the Server will run on
   * @param sizing the Sizing such as pool and buffer sizes
   * @param timing the Timing such as probe interval and missing content timeout
   * @return Server
   */
  public static Server startWith(
          final Stage stage,
          final Resources resources,
          final Filters filters,
          final int port,
          final Sizing sizing,
          final Timing timing) {

    return startWith(stage, resources, filters, port, sizing, timing, "queueMailbox", "queueMailbox");
  }

  /**
   * Answer a new {@code Server} with the given configuration and characteristics.
   * <p>
   * WARNING: The Server has been tested with the {@code "queueMailbox"} mailbox type.
   * This factory method enables you to change that default to another type. If you do
   * change the mailbox type you do so at your own risk.
   *
   * @param stage the Stage in which the Server lives
   * @param resources the Resource with URI descriptions that the Server understands
   * @param filters the Filters used to process requests before dispatching to a resource
   * @param port the int socket port the Server will run on
   * @param sizing the Sizing such as pool and buffer sizes
   * @param timing the Timing such as probe interval and missing content timeout
   * @param severMailboxTypeName the String name of the mailbox to used by the Server
   * @param channelMailboxTypeName the String name of the mailbox to use by the socket channel
   * @return Server
   */
  public static Server startWith(
          final Stage stage,
          final Resources resources,
          final Filters filters,
          final int port,
          final Sizing sizing,
          final Timing timing,
          final String severMailboxTypeName,
          final String channelMailboxTypeName) {

    // this may be overridden by using a different RefreshableSelector
    // initialization that supports a count- or time-based refresh
    // prior to starting the Server.
    RefreshableSelector.withNoThreshold(stage.world().defaultLogger());



    final Server server = stage.actorFor(
            Server.class,
            Definition.has(
                    ServerActor.class,
                    new ServerInstantiator(resources, filters, port, sizing, timing, channelMailboxTypeName),
                    severMailboxTypeName,
                    ServerActor.ServerName),
            stage.world().addressFactory().withHighId(),
            stage.world().defaultLogger());

    server.startUp();

    return server;
  }

  Completes<Boolean> shutDown();
  Completes<Boolean> startUp();

  static class ServerInstantiator implements ActorInstantiator<ServerActor> {
    private static final long serialVersionUID = 1085685844717413620L;

    private final Resources resources;
    private final Filters filters;
    private final int port;
    private final Sizing sizing;
    private final Timing timing;
    private final String channelMailboxTypeName;

    public ServerInstantiator(
            final Resources resources,
            final Filters filters,
            final int port,
            final Sizing sizing,
            final Timing timing,
            final String channelMailboxTypeName) {
      this.resources = resources;
      this.filters = filters;
      this.port = port;
      this.sizing = sizing;
      this.timing = timing;
      this.channelMailboxTypeName = channelMailboxTypeName;
    }

    @Override
    public ServerActor instantiate() {
      try {
        return new ServerActor(resources, filters, port, sizing, timing, channelMailboxTypeName);
      } catch (Exception e) {
        throw new IllegalArgumentException("Failed to instantiate " + type() + " because: " + e.getMessage(), e);
      }
    }

    @Override
    public Class<ServerActor> type() {
      return ServerActor.class;
    }
  }
}
