package com.example.family_map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import Models.Authtoken;
import Models.Event;
import Models.Person;
import RequestResponse.LoginRequest;
import RequestResponse.RegisterRequest;

public class ServerProxyTest {
    private RegisterRequest testRegisterRequest;
    private final String SERVER_HOST = "localhost";
    private final String SERVER_PORT = "8080";

    @Before
    public void setup() {
        DataCache.getInstance().resetCacheData();
        testRegisterRequest = new RegisterRequest(UUID.randomUUID().toString(), "Password",
                "fake@email.com", "Manuel", "Gordillo", "m");
    }

    @Test
    public void registerTaskPositive() {
        //This checks if the the HTTP response is a 200
        assertTrue(ServerProxy.postRegisterUser(SERVER_HOST, SERVER_PORT, testRegisterRequest));
        //For extra surety, check if the user logged in variable has been set to true
        assertTrue(DataCache.getInstance().userLoggedIn);

        //This confirms that the current user has been set
        assertNotNull(DataCache.getInstance().getCurrentPerson());
    }

    @Test
    public void registerTaskNegative() {
        //This checks if the the HTTP response is a 200
        assertTrue(ServerProxy.postRegisterUser(SERVER_HOST, SERVER_PORT, testRegisterRequest));
        assertTrue(DataCache.getInstance().userLoggedIn);

        //This confirms that the current user has been set
        assertNotNull(DataCache.getInstance().getCurrentPerson());

        //This checks if the the HTTP response is a 400, this is because there can't be more than one same user
        assertFalse(ServerProxy.postRegisterUser(SERVER_HOST, SERVER_PORT, testRegisterRequest));
        //For extra surety, check if the user logged in variable has been set to false
        assertFalse(DataCache.getInstance().userLoggedIn);
    }

    @Test
    public void loginTaskPositive() {
        //This checks if the the HTTP response is a 200 and user is registered
        assertTrue(ServerProxy.postRegisterUser(SERVER_HOST, SERVER_PORT, testRegisterRequest));
        assertTrue(DataCache.getInstance().userLoggedIn);

        LoginRequest loginPositiveRequest = new LoginRequest(testRegisterRequest.getUserName(),
                testRegisterRequest.getPassword());

        //If no errors occurred then a 200 request will be returned in the form of a true boolean
        assertTrue(ServerProxy.postLoginUser(SERVER_HOST, SERVER_PORT, loginPositiveRequest));
        //Checks if user logged in variable has been set to true
        assertTrue(DataCache.getInstance().userLoggedIn);
    }

    @Test
    public void loginTaskNegative() {
        LoginRequest invalidLoginRequest = new LoginRequest("Fake", "Password");

        //Checks for a 400 Bad Request
        assertFalse(ServerProxy.postLoginUser(SERVER_HOST, SERVER_PORT, invalidLoginRequest));
        //Checks if the user logged in variable hasn't been set to true
        assertFalse(DataCache.getInstance().userLoggedIn);
    }

    @Test
    public void peopleRelatedToUserPositive() {
        //This checks if the the HTTP response is a 200 and user was logged in
        assertTrue(ServerProxy.postRegisterUser(SERVER_HOST, SERVER_PORT, testRegisterRequest));
        assertTrue(DataCache.getInstance().userLoggedIn);

        Authtoken successAuthtoken = DataCache.getInstance().getCurrentAuthtoken();
        Person currentUser = DataCache.getInstance().getCurrentPerson();

        //Checks if the response if a 200 and verifies that the current user
        // was retrieved from the database with here family
        assertTrue(ServerProxy.getPeopleByUser(SERVER_HOST, SERVER_PORT, successAuthtoken));
        assertTrue(DataCache.getInstance().getFamilyPersonTree().containsKey(currentUser.getPersonID()));
    }

    @Test
    public void peopleRelatedToUserNegative() {
        //This checks if the the HTTP response is a 200 and user was logged in
        assertTrue(ServerProxy.postRegisterUser(SERVER_HOST, SERVER_PORT, testRegisterRequest));
        assertTrue(DataCache.getInstance().userLoggedIn);

        Person currentUser = DataCache.getInstance().getCurrentPerson();
        Authtoken invalidAuthtoken = new Authtoken("this token is bad",
                testRegisterRequest.getUserName());

        //Returns a 400 if the authtoken given does is bad so no
        assertFalse(ServerProxy.getPeopleByUser(SERVER_HOST, SERVER_PORT, invalidAuthtoken));
    }

    @Test
    public void eventsRelatedToUserPositive() {
        //This checks if the the HTTP response is a 200 and user was logged in
        assertTrue(ServerProxy.postRegisterUser(SERVER_HOST, SERVER_PORT, testRegisterRequest));
        assertTrue(DataCache.getInstance().userLoggedIn);

        Authtoken successAuthtoken = DataCache.getInstance().getCurrentAuthtoken();
        Person currentUser = DataCache.getInstance().getCurrentPerson();

        //This means a 200 OK was retrieved
        assertTrue(ServerProxy.getEventsByUser(SERVER_HOST, SERVER_PORT, successAuthtoken));

        //Retrieves the current user's events and checks if that list are his/hers based on the person ID
        List<Event> currentUsersEvents = DataCache.getInstance().getEventsOfPerson().get(currentUser.getPersonID());
        assertEquals(currentUsersEvents.get(0).getPersonID(), currentUser.getPersonID());
    }

    @Test
    public void eventsRelatedToUserNegative() {
        //This checks if the the HTTP response is a 200 and user was logged in
        assertTrue(ServerProxy.postRegisterUser(SERVER_HOST, SERVER_PORT, testRegisterRequest));
        assertTrue(DataCache.getInstance().userLoggedIn);

        Authtoken invalidAuthtoken = new Authtoken("this token is bad",
                testRegisterRequest.getUserName());

        //Returns a 400 if the authtoken given does is bad so no
        assertFalse(ServerProxy.getEventsByUser(SERVER_HOST, SERVER_PORT, invalidAuthtoken));
    }
}
