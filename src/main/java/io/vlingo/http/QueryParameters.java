// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    try {
      final String[] parameters = query.split("&");
      final Map<String, List<String>> queryParameters = new HashMap<>(parameters.length);
      for (final String parameter : parameters) {
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
      return queryParameters;
    } catch (Exception e) {
      throw new IllegalArgumentException("Query parameters invalid: " + query, e);
    }
  }
}
