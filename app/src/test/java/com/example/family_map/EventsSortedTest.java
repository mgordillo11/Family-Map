package com.example.family_map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import Models.Event;
import RequestResponse.LoginRequest;
import RequestResponse.RegisterRequest;

public class EventsSortedTest {
    //The second test, make sure to LOAD the sheila parker data before hand
    //Because it tests for custom event types
    private RegisterRequest testRegisterRequest;
    private final String SERVER_HOST = "localhost";
    private final String SERVER_PORT = "8080";

    @Before
    public void setup() {
        testRegisterRequest = new RegisterRequest(UUID.randomUUID().toString(), "Password",
                "fake@email.com", "Manuel", "Gordillo", "m");
    }

    @Test
    public void searchRandomGenerated() {
        //This checks if the the HTTP response is a 200
        assertTrue(ServerProxy.postRegisterUser(SERVER_HOST, SERVER_PORT, testRegisterRequest));
        //For extra surety, check if the user logged in variable has been set to true
        assertTrue(DataCache.getInstance().userLoggedIn);

        //I'm grabbing the events of the current user's father, since he'll have 3 events for sure
        List<Event> randomUserEvents = DataCache.getInstance().getEventsOfPerson()
                .get(DataCache.getInstance().getCurrentPerson().getFatherID());

        assert randomUserEvents != null;
        Collections.sort(randomUserEvents);

        assertEquals(randomUserEvents.get(0).getEventType(), "Birth");
        assertEquals(randomUserEvents.get(1).getEventType(), "Marriage");
        assertEquals(randomUserEvents.get(2).getEventType(), "Death");
    }

    @Test
    public void searchCustomEvent() {
        LoginRequest loginPositiveRequest = new LoginRequest("sheila",
                "parker");

        //If no errors occurred then a 200 request will be returned in the form of a true boolean
        assertTrue(ServerProxy.postLoginUser(SERVER_HOST, SERVER_PORT, loginPositiveRequest));
        //Checks if user logged in variable has been set to true
        assertTrue(DataCache.getInstance().userLoggedIn);

        List<Event> customUserEventTypes = DataCache.getInstance().getEventsOfPerson()
                .get(DataCache.getInstance().getCurrentPerson().getPersonID());

        assert customUserEventTypes != null;
        Collections.sort(customUserEventTypes);

        assertEquals(customUserEventTypes.get(0).getEventType(), "birth");
        assertEquals(customUserEventTypes.get(1).getEventType(), "marriage");
        assertEquals(customUserEventTypes.get(2).getEventType(), "completed asteroids");
        assertEquals(customUserEventTypes.get(3).getEventType(), "COMPLETED ASTEROIDS");
        assertEquals(customUserEventTypes.get(4).getEventType(), "death");
    }
}
