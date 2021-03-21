# vlingo-http

[![Javadocs](http://javadoc.io/badge/io.vlingo/vlingo-http.svg?color=brightgreen)](http://javadoc.io/doc/io.vlingo/vlingo-http) [![Build](https://github.com/vlingo/vlingo-http/workflows/Build/badge.svg)](https://github.com/vlingo/vlingo-http/actions?query=workflow%3ABuild) [ ![Download](https://api.bintray.com/packages/vlingo/vlingo-platform-java/vlingo-http/images/download.svg) ](https://bintray.com/vlingo/vlingo-platform-java/vlingo-http/_latestVersion) [![Gitter chat](https://badges.gitter.im/gitterHQ/gitter.png)](https://gitter.im/vlingo-platform-java/http)

The VLINGO XOOM platform SDK Reactive, scalable, high-throughput, and resilient HTTP server supporting RESTful services running on XOOM LATTICE and XOOM ACTORS.

Docs: https://docs.vlingo.io/vlingo-http

### Important
If using snapshot builds [follow these instructions](https://github.com/vlingo/vlingo-platform#snapshots-repository) or you will experience failures.

### Bintray

```xml
  <repositories>
    <repository>
      <id>jcenter</id>
      <url>https://jcenter.bintray.com/</url>
    </repository>
  </repositories>
  <dependencies>
    <dependency>
      <groupId>io.vlingo</groupId>
      <artifactId>vlingo-http</artifactId>
      <version>1.5.0</version>
      <scope>compile</scope>
    </dependency>
  </dependencies>
```

```gradle
dependencies {
    compile 'io.vlingo:vlingo-http:1.5.0'
}

repositories {
    jcenter()
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
  * [Use Server#startWith() to start the Server actor](https://github.com/vlingo/vlingo-http/blob/master/src/main/java/io/vlingo/http/resource/Server.java)
  * The light-qualityFactor Server is meant to be run inside vlingo/cluster nodes the require RESTful HTTP support.
3. See the following for usage examples:
  * [vlingo/http properties file](https://github.com/vlingo/vlingo-http/blob/master/src/test/resources/vlingo-http.properties)
  * [The user resource sample](https://github.com/vlingo/vlingo-http/blob/master/src/main/java/io/vlingo/http/sample/user/UserResource.java)
  * [The user profile resource sample](https://github.com/vlingo/vlingo-http/blob/master/src/main/java/io/vlingo/http/sample/user/ProfileResource.java)

License (See LICENSE file for full license)
-------------------------------------------
Copyright © 2012-2020 VLINGO LABS. All rights reserved.

This Source Code Form is subject to the terms of the
Mozilla Public License, v. 2.0. If a copy of the MPL
was not distributed with this file, You can obtain
one at https://mozilla.org/MPL/2.0/.
