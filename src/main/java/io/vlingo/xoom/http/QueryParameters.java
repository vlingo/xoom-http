// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.http;

import java.net.URLDecoder;
import java.util.*;

public class QueryParameters {
  private final Map<String,List<String>> allParameters;

  public QueryParameters(final String query) {
    this.allParameters = parseQuery(query);
  }

  public Collection<String> names() {
    return allParameters.keySet();
  }

  public List<String> valuesOf(final String name) {
    return Collections.unmodifiableList(allParameters.get(name));
  }

  private Map<String, List<String>> parseQuery(final String query) {
    final Map<String, List<String>> queryParameters;
    if (query == null || query.isEmpty()) {
      queryParameters = new HashMap<>();
    }
    else {
      try {
        final String[] parameters = query.split("&");
        queryParameters = new HashMap<>(parameters.length);
        for (final String parameter : parameters) {
          if (parameter.isEmpty()) continue;
          final int equalSign = parameter.indexOf("=");
          final String name = equalSign > 0 ?
                  URLDecoder.decode(parameter.substring(0, equalSign), "UTF-8") :
                  parameter;
          final String value = equalSign > 0 && parameter.length() > equalSign + 1 ?
                  URLDecoder.decode(parameter.substring(equalSign + 1), "UTF-8") :
                  null;
          if (!queryParameters.containsKey(name)) {
            queryParameters.put(name, new ArrayList<String>(2));
          }
          queryParameters.get(name).add(value);
        }
      } catch (Exception e) {
        throw new IllegalArgumentException("Query parameters invalid: " + query, e);
      }
    }
    return queryParameters;
  }

  public boolean containsKey(final String key) {
    return allParameters.containsKey(key);
  }
}
