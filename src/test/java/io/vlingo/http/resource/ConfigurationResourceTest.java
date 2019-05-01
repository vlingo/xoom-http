// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource;

import static io.vlingo.common.serialization.JsonSerialization.deserialized;
import static io.vlingo.common.serialization.JsonSerialization.deserializedList;
import static io.vlingo.common.serialization.JsonSerialization.serialized;
import static io.vlingo.http.Response.Status.Created;
import static io.vlingo.http.Response.Status.Ok;
import static io.vlingo.http.ResponseHeader.Location;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.google.gson.reflect.TypeToken;

import io.vlingo.actors.testkit.TestUntil;
import io.vlingo.http.Context;
import io.vlingo.http.Method;
import io.vlingo.http.Request;
import io.vlingo.http.resource.Action.MatchResults;
import io.vlingo.http.sample.user.NameData;
import io.vlingo.http.sample.user.UserData;

public class ConfigurationResourceTest extends ResourceTestFixtures {

  @Test
  public void testThatPostRegisterUserDispatches() {
    final Request request = Request.from(toByteBuffer(postJohnDoeUserMessage));
    final MockCompletesEventuallyResponse completes = new MockCompletesEventuallyResponse();
    
    MockCompletesEventuallyResponse.untilWith = TestUntil.happenings(1);
    dispatcher.dispatchFor(new Context(request, completes));
    MockCompletesEventuallyResponse.untilWith.completes();
    
    assertNotNull(completes.response);
    
    assertEquals(Created, completes.response.status);
    assertEquals(2, completes.response.headers.size());
    assertEquals(Location, completes.response.headers.get(0).name);
    assertTrue(Location, completes.response.headerOf(Location).value.startsWith("/users/"));
    assertNotNull(completes.response.entity);
    
    final UserData createdUserData = deserialized(completes.response.entity.content, UserData.class);
    assertNotNull(createdUserData);
    assertEquals(johnDoeUserData.nameData.given, createdUserData.nameData.given);
    assertEquals(johnDoeUserData.nameData.family, createdUserData.nameData.family);
    assertEquals(johnDoeUserData.contactData.emailAddress, createdUserData.contactData.emailAddress);
    assertEquals(johnDoeUserData.contactData.telephoneNumber, createdUserData.contactData.telephoneNumber);
  }

  @Test
  public void testThatGetUserDispatches() {
    final Request postRequest = Request.from(toByteBuffer(postJohnDoeUserMessage));
    final MockCompletesEventuallyResponse postCompletes = new MockCompletesEventuallyResponse();
    MockCompletesEventuallyResponse.untilWith = TestUntil.happenings(1);
    dispatcher.dispatchFor(new Context(postRequest, postCompletes));
    MockCompletesEventuallyResponse.untilWith.completes();
    assertNotNull(postCompletes.response);
    
    final String getUserMessage = "GET " + postCompletes.response.headerOf(Location).value + " HTTP/1.1\nHost: vlingo.io\n\n";
    final Request getRequest = Request.from(toByteBuffer(getUserMessage));
    final MockCompletesEventuallyResponse getCompletes = new MockCompletesEventuallyResponse();
    MockCompletesEventuallyResponse.untilWith = TestUntil.happenings(1);
    dispatcher.dispatchFor(new Context(getRequest, getCompletes));
    MockCompletesEventuallyResponse.untilWith.completes();
    assertNotNull(getCompletes.response);
    assertEquals(Ok, getCompletes.response.status);
    final UserData getUserData = deserialized(getCompletes.response.entity.content, UserData.class);
    assertNotNull(getUserData);
    assertEquals(johnDoeUserData.nameData.given, getUserData.nameData.given);
    assertEquals(johnDoeUserData.nameData.family, getUserData.nameData.family);
    assertEquals(johnDoeUserData.contactData.emailAddress, getUserData.contactData.emailAddress);
    assertEquals(johnDoeUserData.contactData.telephoneNumber, getUserData.contactData.telephoneNumber);
  }

  @Test
  public void testThatGetAllUsersDispatches() {
    final Request postRequest1 = Request.from(toByteBuffer(postJohnDoeUserMessage));
    final MockCompletesEventuallyResponse postCompletes1 = new MockCompletesEventuallyResponse();
    
    MockCompletesEventuallyResponse.untilWith = TestUntil.happenings(1);
    dispatcher.dispatchFor(new Context(postRequest1, postCompletes1));
    MockCompletesEventuallyResponse.untilWith.completes();

    assertNotNull(postCompletes1.response);
    final Request postRequest2 = Request.from(toByteBuffer(postJaneDoeUserMessage));
    final MockCompletesEventuallyResponse postCompletes2 = new MockCompletesEventuallyResponse();

    MockCompletesEventuallyResponse.untilWith = TestUntil.happenings(1);
    dispatcher.dispatchFor(new Context(postRequest2, postCompletes2));
    MockCompletesEventuallyResponse.untilWith.completes();

    assertNotNull(postCompletes2.response);
    
    final String getUserMessage = "GET /users HTTP/1.1\nHost: vlingo.io\n\n";
    final Request getRequest = Request.from(toByteBuffer(getUserMessage));
    final MockCompletesEventuallyResponse getCompletes = new MockCompletesEventuallyResponse();

    MockCompletesEventuallyResponse.untilWith = TestUntil.happenings(1);
    dispatcher.dispatchFor(new Context(getRequest, getCompletes));
    MockCompletesEventuallyResponse.untilWith.completes();
    
    assertNotNull(getCompletes.response);
    assertEquals(Ok, getCompletes.response.status);
    final Type listOfUserData = new TypeToken<List<UserData>>(){}.getType();
    final List<UserData> getUserData = deserializedList(getCompletes.response.entity.content, listOfUserData);
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
    final MockCompletesEventuallyResponse postCompletes1 = new MockCompletesEventuallyResponse();
    MockCompletesEventuallyResponse.untilWith = TestUntil.happenings(1);
    dispatcher.dispatchFor(new Context(postRequest1, postCompletes1));
    MockCompletesEventuallyResponse.untilWith.completes();

    assertNotNull(postCompletes1.response);
    
    final Request postRequest2 = Request.from(toByteBuffer(postJaneDoeUserMessage));
    final MockCompletesEventuallyResponse postCompletes2 = new MockCompletesEventuallyResponse();
    
    MockCompletesEventuallyResponse.untilWith = TestUntil.happenings(1);
    dispatcher.dispatchFor(new Context(postRequest2, postCompletes2));
    MockCompletesEventuallyResponse.untilWith.completes();

    assertNotNull(postCompletes2.response);
    
    // John Doe and Jane Doe marry and change their family name to, of course, Doe-Doe
    final NameData johnNameData = NameData.from("John", "Doe-Doe");
    final String johnNameSerialized = serialized(johnNameData);
    final String patchJohnDoeUserMessage =
            "PATCH " + postCompletes1.response.headerOf(Location).value
            + "/name HTTP/1.1\nHost: vlingo.io\nContent-Length: " + johnNameSerialized.length()
            + "\n\n" + johnNameSerialized;
    
    final Request patchRequest1 = Request.from(toByteBuffer(patchJohnDoeUserMessage));
    final MockCompletesEventuallyResponse patchCompletes1 = new MockCompletesEventuallyResponse();

    MockCompletesEventuallyResponse.untilWith = TestUntil.happenings(1);
    dispatcher.dispatchFor(new Context(patchRequest1, patchCompletes1));
    MockCompletesEventuallyResponse.untilWith.completes();

    assertNotNull(patchCompletes1.response);
    assertEquals(Ok, patchCompletes1.response.status);
    final UserData getJohnDoeDoeUserData = deserialized(patchCompletes1.response.entity.content, UserData.class);
    assertEquals(johnNameData.given, getJohnDoeDoeUserData.nameData.given);
    assertEquals(johnNameData.family, getJohnDoeDoeUserData.nameData.family);
    assertEquals(johnDoeUserData.contactData.emailAddress, getJohnDoeDoeUserData.contactData.emailAddress);
    assertEquals(johnDoeUserData.contactData.telephoneNumber, getJohnDoeDoeUserData.contactData.telephoneNumber);

    final NameData janeNameData = NameData.from("Jane", "Doe-Doe");
    final String janeNameSerialized = serialized(janeNameData);
    final String patchJaneDoeUserMessage =
            "PATCH " + postCompletes2.response.headerOf(Location).value
            + "/name HTTP/1.1\nHost: vlingo.io\nContent-Length: " + janeNameSerialized.length()
            + "\n\n" + janeNameSerialized;

    final Request patchRequest2 = Request.from(toByteBuffer(patchJaneDoeUserMessage));
    final MockCompletesEventuallyResponse patchCompletes2 = new MockCompletesEventuallyResponse();
    
    MockCompletesEventuallyResponse.untilWith = TestUntil.happenings(1);
    dispatcher.dispatchFor(new Context(patchRequest2, patchCompletes2));
    MockCompletesEventuallyResponse.untilWith.completes();

    assertNotNull(patchCompletes2.response);
    assertEquals(Ok, patchCompletes2.response.status);
    final UserData getJaneDoeDoeUserData = deserialized(patchCompletes2.response.entity.content, UserData.class);
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
    actionPostUser = new Action(0, "POST", "/users", "register(body:io.vlingo.http.sample.user.UserData userData)", null, true);
    actionPatchUserContact = new Action(1, "PATCH", "/users/{userId}/contact", "changeContact(String userId, body:io.vlingo.http.sample.user.ContactData contactData)", null, true);
    actionPatchUserName = new Action(2, "PATCH", "/users/{userId}/name", "changeName(String userId, body:io.vlingo.http.sample.user.NameData nameData)", null, true);
    actionGetUsers = new Action(3, "GET", "/users", "queryUsers()", null, true);
    actionGetUser = new Action(4, "GET", "/users/{userId}", "queryUser(String userId)", null, true);
    final Action actionGetUserEmailAddress = new Action(5, "GET", "/users/{userId}/emailAddresses/{emailAddressId}", "queryUserEmailAddress(String userId, String emailAddressId)", null, true);

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
    //   userResource.NAME.disallowPathParametersWithSlash = true/false
    //=============================================================
    
    final List<Action> actions =
            Arrays.asList(
                    actionPostUser,
                    actionPatchUserContact,
                    actionPatchUserName,
                    actionGetUsers, // order is problematic unless parameter matching short circuit used
                    actionGetUser,
                    actionGetUserEmailAddress);
    
    final ConfigurationResource<?> resource = ConfigurationResource.newResourceFor("user", resourceHandlerClass, 5, actions);

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
}
