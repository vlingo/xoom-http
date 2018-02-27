// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.sample.user;

import java.util.List;
import java.util.function.Consumer;

import io.vlingo.http.Context;
import io.vlingo.http.resource.Action;
import io.vlingo.http.resource.Action.MappedParameters;
import io.vlingo.http.resource.Resource;

public class ProfileResourceDispatcher extends Resource<ProfileResource> {

  public ProfileResourceDispatcher(
          final String name,
          final String resourceHandlerClassname,
          final int handlerPoolSize,
          final List<Action> actions) {
    super(name, resourceHandlerClassname, handlerPoolSize, actions);
  }

  @Override
  public void dispatchToHandlerWith(final Context context, final MappedParameters mappedParameters) {
    Consumer<ProfileResource> consumer = null;
    
    switch (mappedParameters.actionId) {
    case 0: // PUT /users/{userId}/profile define(String userId, body:io.vlingo.http.sample.user.ProfileData profileData)
      consumer = (handler) -> handler.define((String) mappedParameters.mapped.get(0).value, (ProfileData) mappedParameters.mapped.get(1).value);
      break;
    case 1: // GET /users/{userId}/profile query(String userId)
      consumer = (handler) -> handler.query((String) mappedParameters.mapped.get(0).value);
      break;
    }
    
    pooledHandler().handleFor(context, consumer);
  }
}
