// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import static io.vlingo.http.Response.Created;
import static io.vlingo.http.Response.Ok;
import static io.vlingo.http.ResponseHeader.Location;
import static io.vlingo.http.resource.serialization.JsonSerialization.deserialized;
import static io.vlingo.http.resource.serialization.JsonSerialization.deserializedList;
import static io.vlingo.http.resource.serialization.JsonSerialization.serialized;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.reflect.TypeToken;

import io.vlingo.actors.World;
import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.http.BaseTest;
import io.vlingo.http.Context;
import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.resource.Action.MatchResults;
import io.vlingo.http.sample.user.ContactData;
import io.vlingo.http.sample.user.NameData;
import io.vlingo.http.sample.user.UserData;
import io.vlingo.http.sample.user.model.UserRepository;

public class ResourceTest extends BaseTest {
  private Action actionPostUser;
  private Action actionPatchUserContact;
  private Action actionPatchUserName;
  private Action actionGetUser;
  private Action actionGetUsers;

  private World world;
  private Resource<?> resource;
  private Class<? extends ResourceHandler> resourceHandlerClass;
  private Resources resources;
  private Dispatcher dispatcher;
  private Server server;
  
  private final UserData johnDoeUserData =
          UserData.from(
                  NameData.from("John", "Doe"),
                  ContactData.from("john.doe@vlingo.io", "+1 212-555-1212"));

  private final UserData janeDoeUserData =
          UserData.from(
                  NameData.from("Jane", "Doe"),
                  ContactData.from("jane.doe@vlingo.io", "+1 212-555-1212"));

  private final String postJohnDoeUserMessage =
          "POST /users HTTP/1.1\nHost: vlingo.io\n\n" + serialized(johnDoeUserData);

  private final String postJaneDoeUserMessage =
          "POST /users HTTP/1.1\nHost: vlingo.io\n\n" + serialized(janeDoeUserData);

  @Test
  public void testThatPostRegisterUserDispatches() {
    final Request request = Request.from(toByteBuffer(postJohnDoeUserMessage));
    final MockCompletesResponse completes = new MockCompletesResponse();
    
    MockCompletesResponse.untilWith = TestUntil.happenings(1);
    server.dispatchFor(new Context(request, completes));
    MockCompletesResponse.untilWith.completes();
    
    assertNotNull(completes.response);
    
    assertEquals(Created, completes.response.statusCode);
    assertEquals(1, completes.response.headers.size());
    assertEquals(Location, completes.response.headers.get(0).name);
    assertTrue(Location, completes.response.headerOf(Location).value.startsWith("/users/"));
    assertNotNull(completes.response.entity);
    
    final UserData createdUserData = deserialized(completes.response.entity, UserData.class);
    assertNotNull(createdUserData);
    assertEquals(johnDoeUserData.nameData.given, createdUserData.nameData.given);
    assertEquals(johnDoeUserData.nameData.family, createdUserData.nameData.family);
    assertEquals(johnDoeUserData.contactData.emailAddress, createdUserData.contactData.emailAddress);
    assertEquals(johnDoeUserData.contactData.telephoneNumber, createdUserData.contactData.telephoneNumber);
  }

  @Test
  public void testThatGetUserDispatches() {
    final Request postRequest = Request.from(toByteBuffer(postJohnDoeUserMessage));
    final MockCompletesResponse postCompletes = new MockCompletesResponse();
    
    MockCompletesResponse.untilWith = TestUntil.happenings(1);
    server.dispatchFor(new Context(postRequest, postCompletes));
    MockCompletesResponse.untilWith.completes();
    
    assertNotNull(postCompletes.response);
    
    final String getUserMessage = "GET " + postCompletes.response.headerOf(Location).value + " HTTP/1.1\nHost: vlingo.io\n\n";
    final Request getRequest = Request.from(toByteBuffer(getUserMessage));
    final MockCompletesResponse getCompletes = new MockCompletesResponse();
    
    MockCompletesResponse.untilWith = TestUntil.happenings(1);
    server.dispatchFor(new Context(getRequest, getCompletes));
    MockCompletesResponse.untilWith.completes();
    
    assertNotNull(getCompletes.response);
    assertEquals(Ok, getCompletes.response.statusCode);
    final UserData getUserData = deserialized(getCompletes.response.entity, UserData.class);
    assertNotNull(getUserData);
    assertEquals(johnDoeUserData.nameData.given, getUserData.nameData.given);
    assertEquals(johnDoeUserData.nameData.family, getUserData.nameData.family);
    assertEquals(johnDoeUserData.contactData.emailAddress, getUserData.contactData.emailAddress);
    assertEquals(johnDoeUserData.contactData.telephoneNumber, getUserData.contactData.telephoneNumber);
  }

  @Test
  public void testThatGetAllUsersDispatches() {
    final Request postRequest1 = Request.from(toByteBuffer(postJohnDoeUserMessage));
    final MockCompletesResponse postCompletes1 = new MockCompletesResponse();
    
    MockCompletesResponse.untilWith = TestUntil.happenings(1);
    server.dispatchFor(new Context(postRequest1, postCompletes1));
    MockCompletesResponse.untilWith.completes();

    assertNotNull(postCompletes1.response);
    final Request postRequest2 = Request.from(toByteBuffer(postJaneDoeUserMessage));
    final MockCompletesResponse postCompletes2 = new MockCompletesResponse();

    MockCompletesResponse.untilWith = TestUntil.happenings(1);
    server.dispatchFor(new Context(postRequest2, postCompletes2));
    MockCompletesResponse.untilWith.completes();

    assertNotNull(postCompletes2.response);
    
    final String getUserMessage = "GET /users HTTP/1.1\nHost: vlingo.io\n\n";
    final Request getRequest = Request.from(toByteBuffer(getUserMessage));
    final MockCompletesResponse getCompletes = new MockCompletesResponse();

    MockCompletesResponse.untilWith = TestUntil.happenings(1);
    server.dispatchFor(new Context(getRequest, getCompletes));
    MockCompletesResponse.untilWith.completes();
    
    assertNotNull(getCompletes.response);
    assertEquals(Ok, getCompletes.response.statusCode);
    final Type listOfUserData = new TypeToken<List<UserData>>(){}.getType();
    final List<UserData> getUserData = deserializedList(getCompletes.response.entity, listOfUserData);
    assertNotNull(getUserData);
    
    final UserData johnUserData = UserData.userAt(postCompletes1.response.headerOf(Location).value, getUserData);
    
    assertEquals(johnDoeUserData.nameData.given, johnUserData.nameData.given);
    assertEquals(johnDoeUserData.nameData.family, johnUserData.nameData.family);
    assertEquals(johnDoeUserData.contactData.emailAddress, johnUserData.contactData.emailAddress);
    assertEquals(johnDoeUserData.contactData.telephoneNumber, johnUserData.contactData.telephoneNumber);
    
    final UserData janeUserData = UserData.userAt(postCompletes2.response.headerOf(Location).value, getUserData);
    
    assertEquals(janeDoeUserData.nameData.given, janeUserData.nameData.given);
    assertEquals(janeDoeUserData.nameData.family, janeUserData.nameData.family);
    assertEquals(janeDoeUserData.contactData.emailAddress, janeUserData.contactData.emailAddress);
    assertEquals(janeDoeUserData.contactData.telephoneNumber, janeUserData.contactData.telephoneNumber);
  }
  
  @Test
  public void testThatPatchNameWorks() {
    final Request postRequest1 = Request.from(toByteBuffer(postJohnDoeUserMessage));
    final MockCompletesResponse postCompletes1 = new MockCompletesResponse();
    
    MockCompletesResponse.untilWith = TestUntil.happenings(1);
    server.dispatchFor(new Context(postRequest1, postCompletes1));
    MockCompletesResponse.untilWith.completes();
    
    assertNotNull(postCompletes1.response);
    
    final Request postRequest2 = Request.from(toByteBuffer(postJaneDoeUserMessage));
    final MockCompletesResponse postCompletes2 = new MockCompletesResponse();
    
    MockCompletesResponse.untilWith = TestUntil.happenings(1);
    server.dispatchFor(new Context(postRequest2, postCompletes2));
    MockCompletesResponse.untilWith.completes();

    assertNotNull(postCompletes2.response);
    
    // John Doe and Jane Doe marry and change their family name to, of course, Doe-Doe
    final NameData johnNameData = NameData.from("John", "Doe-Doe");
    final String patchJohnDoeUserMessage =
            "PATCH " + postCompletes1.response.headerOf(Location).value + "/name HTTP/1.1\nHost: vlingo.io\n\n" + serialized(johnNameData);
    final Request patchRequest1 = Request.from(toByteBuffer(patchJohnDoeUserMessage));
    final MockCompletesResponse patchCompletes1 = new MockCompletesResponse();
    
    MockCompletesResponse.untilWith = TestUntil.happenings(1);
    server.dispatchFor(new Context(patchRequest1, patchCompletes1));
    MockCompletesResponse.untilWith.completes();

    assertNotNull(patchCompletes1.response);
    assertEquals(Ok, patchCompletes1.response.statusCode);
    final UserData getJohnDoeDoeUserData = deserialized(patchCompletes1.response.entity, UserData.class);
    assertEquals(johnNameData.given, getJohnDoeDoeUserData.nameData.given);
    assertEquals(johnNameData.family, getJohnDoeDoeUserData.nameData.family);
    assertEquals(johnDoeUserData.contactData.emailAddress, getJohnDoeDoeUserData.contactData.emailAddress);
    assertEquals(johnDoeUserData.contactData.telephoneNumber, getJohnDoeDoeUserData.contactData.telephoneNumber);

    final NameData janeNameData = NameData.from("Jane", "Doe-Doe");
    final String patchJaneDoeUserMessage =
            "PATCH " + postCompletes2.response.headerOf(Location).value + "/name HTTP/1.1\nHost: vlingo.io\n\n" + serialized(janeNameData);
    final Request patchRequest2 = Request.from(toByteBuffer(patchJaneDoeUserMessage));
    final MockCompletesResponse patchCompletes2 = new MockCompletesResponse();
    
    MockCompletesResponse.untilWith = TestUntil.happenings(1);
    server.dispatchFor(new Context(patchRequest2, patchCompletes2));
    MockCompletesResponse.untilWith.completes();

    assertNotNull(patchCompletes2.response);
    assertEquals(Ok, patchCompletes2.response.statusCode);
    final UserData getJaneDoeDoeUserData = deserialized(patchCompletes2.response.entity, UserData.class);
    assertEquals(janeNameData.given, getJaneDoeDoeUserData.nameData.given);
    assertEquals(janeNameData.family, getJaneDoeDoeUserData.nameData.family);
    assertEquals(janeDoeUserData.contactData.emailAddress, getJaneDoeDoeUserData.contactData.emailAddress);
    assertEquals(janeDoeUserData.contactData.telephoneNumber, getJaneDoeDoeUserData.contactData.telephoneNumber);
  }
  
  @Test
  public void testThatAllWellOrderedActionHaveMatches() throws Exception {
    final MatchResults actionGetUsersMatch = resource.matchWith(Method.GET, new URI("/users"));
    assertTrue(actionGetUsersMatch.isMatched());
    assertEquals(actionGetUsers, actionGetUsersMatch.action);

    final MatchResults actionGetUserMatch = resource.matchWith(Method.GET, new URI("/users/1234567"));
    assertTrue(actionGetUserMatch.isMatched());
    assertEquals(actionGetUser, actionGetUserMatch.action);

    final MatchResults actionPatchUserNameMatch = resource.matchWith(Method.PATCH, new URI("/users/1234567/name"));
    assertTrue(actionPatchUserNameMatch.isMatched());
    assertEquals(actionPatchUserName, actionPatchUserNameMatch.action);

    final MatchResults actionPostUserMatch = resource.matchWith(Method.POST, new URI("/users"));
    assertTrue(actionPostUserMatch.isMatched());
    assertEquals(actionPostUser, actionPostUserMatch.action);
  }

  @Test
  public void testThatAllPoorlyOrderedActionHaveMatches() throws Exception {
    final Action actionPostUser = new Action(0, "POST", "/users", "register(body:io.vlingo.http.sample.user.UserData userData)", null, true);
    final Action actionPatchUserName = new Action(1, "PATCH", "/users/{userId}/name", "changeName(String userId)", null, true);
    final Action actionGetUserEmailAddress = new Action(2, "GET", "/users/{userId}/emailAddresses/{emailAddressId}", "queryUserEmailAddress(String userId, String emailAddressId)", null, true);
    final Action actionGetUser = new Action(3, "GET", "/users/{userId}", "queryUser(String userId)", null, true);
    final Action actionGetUsers = new Action(4, "GET", "/users", "queryUsers()", null, true);

    //=============================================================
    // this test assures that the optional feature used in the
    // Action.MatchResults constructor is enabled and short circuits
    // a match if any parameters contain a "/", which would normally
    // mean that the Action that appeared to match didn't have
    // enough Matchable PathSegments. Look for the following in
    // Action.MatchResult(): disallowPathParametersWithSlash
    // see the above testThatAllWellOrderedActionHaveMatches() for
    // a better ordering of actions and one that does not use the
    // disallowPathParametersWithSlash option.
    // See also: vlingo-http.properties
    //   resource.NAME.disallowPathParametersWithSlash = true/false
    //=============================================================
    
    final List<Action> actions =
            Arrays.asList(
                    actionPostUser,
                    actionPatchUserName,
                    actionGetUsers, // order is problematic unless parameter matching short circuit used
                    actionGetUser,
                    actionGetUserEmailAddress);
    
    final Resource<?> resource = Resource.newResourceFor("user", resourceHandlerClass, 5, actions);

    final MatchResults actionGetUsersMatch = resource.matchWith(Method.GET, new URI("/users"));
    assertTrue(actionGetUsersMatch.isMatched());
    assertEquals(actionGetUsers, actionGetUsersMatch.action);

    final MatchResults actionGetUserMatch = resource.matchWith(Method.GET, new URI("/users/1234567"));
    assertTrue(actionGetUserMatch.isMatched());
    assertEquals(actionGetUser, actionGetUserMatch.action);

    final MatchResults actionGetUserEmailAddressMatch = resource.matchWith(Method.GET, new URI("/users/1234567/emailAddresses/890"));
    assertTrue(actionGetUserEmailAddressMatch.isMatched());
    assertEquals(actionGetUserEmailAddress, actionGetUserEmailAddressMatch.action);

    final MatchResults actionPatchUserNameMatch = resource.matchWith(Method.PATCH, new URI("/users/1234567/name"));
    assertTrue(actionPatchUserNameMatch.isMatched());
    assertEquals(actionPatchUserName, actionPatchUserNameMatch.action);

    final MatchResults actionPostUserMatch = resource.matchWith(Method.POST, new URI("/users"));
    assertTrue(actionPostUserMatch.isMatched());
    assertEquals(actionPostUser, actionPostUserMatch.action);
  }
  
  @Before
  public void setUp() {
    world = World.start("test-resource");
    
    actionPostUser = new Action(0, "POST", "/users", "register(body:io.vlingo.http.sample.user.UserData userData)", null, true);
    actionPatchUserContact = new Action(1, "PATCH", "/users/{userId}/contact", "changeContact(String userId, body:io.vlingo.http.sample.user.ContactData contactData)", null, true);
    actionPatchUserName = new Action(2, "PATCH", "/users/{userId}/name", "changeName(String userId, body:io.vlingo.http.sample.user.NameData nameData)", null, true);
    actionGetUser = new Action(3, "GET", "/users/{userId}", "queryUser(String userId)", null, true);
    actionGetUsers = new Action(4, "GET", "/users", "queryUsers()", null, true);

    final List<Action> actions =
            Arrays.asList(
                    actionPostUser,
                    actionPatchUserContact,
                    actionPatchUserName,
                    actionGetUser,
                    actionGetUsers);

    resourceHandlerClass = Resource.newResourceHandlerClassFor("io.vlingo.http.sample.user.UserResource");
    
    resource = Resource.newResourceFor("user", resourceHandlerClass, 5, actions);
    
    final Map<String,Resource<?>> oneResource = new HashMap<>(1);
    
    oneResource.put(resource.name, resource);
    
    resources = new Resources(oneResource);
    
    dispatcher = Dispatcher.startWith(world.stage(), resources);
    
    server = Server.startWith(dispatcher);
  }
  
  @After
  public void tearDown() {
    server.stop();
    
    world.terminate();
    
    UserRepository.reset();
  }
}
