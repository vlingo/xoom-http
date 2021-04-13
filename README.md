# xoom-http

[![Javadocs](http://javadoc.io/badge/io.vlingo.xoom/xoom-http.svg?color=brightgreen)](http://javadoc.io/doc/io.vlingo.xoom/xoom-http) [![Build](https://github.com/vlingo/xoom-http/workflows/Build/badge.svg)](https://github.com/vlingo/xoom-http/actions?query=workflow%3ABuild) [![Download](https://img.shields.io/maven-central/v/io.vlingo.xoom/xoom-http?label=maven)](https://search.maven.org/artifact/io.vlingo.xoom/xoom-http) [![Gitter chat](https://badges.gitter.im/gitterHQ/gitter.png)](https://gitter.im/vlingo-platform-java/http)

The VLINGO XOOM platform SDK Reactive, scalable, high-throughput, and resilient HTTP server supporting RESTful services running on XOOM LATTICE and XOOM ACTORS.

Docs: https://docs.vlingo.io/xoom-http

### Installation

```xml
  <dependencies>
    <dependency>
      <groupId>io.vlingo.xoom</groupId>
      <artifactId>xoom-http</artifactId>
      <version>1.6.0</version>
      <scope>compile</scope>
    </dependency>
  </dependencies>
```

```gradle
dependencies {
    compile 'io.vlingo.xoom:xoom-http:1.6.0'
}
```

### Usage

Reactive, scalable, and resilient HTTP servers and RESTful services running on XOOM LATTICE and XOOM ACTORS.

1. Feature complete
  * Fully actor-based asynchronous requests and responses.
  * Request handling is resource based.
  * Requests that require message body content are auto-mapped to simple Java objects.
  * Supports Media Types, Filters
  * Supports Server-Sent Events [See SSE on Wikipedia](https://en.wikipedia.org/wiki/Server-sent_events)
2. To run the Server:
  * [Use Server#startWith() to start the Server actor](https://github.com/vlingo/xoom-http/blob/master/src/main/java/io/vlingo/xoom/http/resource/Server.java)
  * The light-qualityFactor Server is meant to be run inside VLINGO XOOM Cluster nodes that require RESTful HTTP support.
3. See the following for usage examples:
  * [VLINGO XOOM Http properties file](https://github.com/vlingo/xoom-http/blob/master/src/test/resources/xoom-http.properties)
  * [The user resource sample](https://github.com/vlingo/xoom-http/blob/master/src/main/java/io/vlingo/xoom/http/sample/user/UserResource.java)
  * [The user profile resource sample](https://github.com/vlingo/xoom-http/blob/master/src/main/java/io/vlingo/xoom/http/sample/user/ProfileResource.java)

License (See LICENSE file for full license)
-------------------------------------------
Copyright © 2012-2021 VLINGO LABS. All rights reserved.

This Source Code Form is subject to the terms of the
Mozilla Public License, v. 2.0. If a copy of the MPL
was not distributed with this file, You can obtain
one at https://mozilla.org/MPL/2.0/.
