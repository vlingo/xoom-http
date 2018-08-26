package io.vlingo.http.sample.user;

import java.util.List;
import java.util.function.Consumer;

import io.vlingo.http.Context;
import io.vlingo.http.resource.Action;
import io.vlingo.http.resource.Action.MappedParameters;
import io.vlingo.http.resource.ConfigurationResource;
import io.vlingo.http.resource.ResourceHandler;

public class UserResourceDispatcher extends ConfigurationResource<UserResource> {

  public UserResourceDispatcher(
          final String name,
          final Class<? extends ResourceHandler> resourceHandlerClass,
          final int handlerPoolSize,
          final List<Action> actions) {
    super(name, resourceHandlerClass, handlerPoolSize, actions);
  }

  @Override
  public void dispatchToHandlerWith(final Context context, final MappedParameters mappedParameters) {
    Consumer<UserResource> consumer = null;

    try {
      switch (mappedParameters.actionId) {
      case 0: // POST /users register(body:io.vlingo.http.sample.user.UserData userData)
        consumer = (handler) -> handler.register((io.vlingo.http.sample.user.UserData) mappedParameters.mapped.get(0).value);
        pooledHandler().handleFor(context, consumer);
        System.out.print("P");
        break;
      case 1: // PATCH /users/{userId}/contact changeContact(String userId, body:io.vlingo.http.sample.user.ContactData contactData)
        consumer = (handler) -> handler.changeContact((String) mappedParameters.mapped.get(0).value, (io.vlingo.http.sample.user.ContactData) mappedParameters.mapped.get(1).value);
        pooledHandler().handleFor(context, consumer);
        break;
      case 2: // PATCH /users/{userId}/name changeName(String userId, body:io.vlingo.http.sample.user.NameData nameData)
        consumer = (handler) -> handler.changeName((String) mappedParameters.mapped.get(0).value, (io.vlingo.http.sample.user.NameData) mappedParameters.mapped.get(1).value);
        pooledHandler().handleFor(context, consumer);
        break;
      case 3: // GET /users/{userId} queryUser(String userId)
        consumer = (handler) -> handler.queryUser((String) mappedParameters.mapped.get(0).value);
        pooledHandler().handleFor(context, consumer);
        break;
      case 4: // GET /users queryUsers()
        consumer = (handler) -> handler.queryUsers();
        pooledHandler().handleFor(context, consumer);
        break;
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Action mismatch: Request: " + context.request + "Parameters: " + mappedParameters);
    }
  }
}
