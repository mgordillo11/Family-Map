package com.example.family_map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import Models.Event;
import Models.Person;
import RequestResponse.RegisterRequest;

public class FilterSettingsTest {
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
    public void variousSettings() {
        //This checks if the the HTTP response is a 200
        assertTrue(ServerProxy.postRegisterUser(SERVER_HOST, SERVER_PORT, testRegisterRequest));
        //For extra surety, check if the user logged in variable has been set to true
        assertTrue(DataCache.getInstance().userLoggedIn);

        //Test for random user's Dad not being on the map
        DataCache.getSettings().maleEvents = false;
        DataCache.getSettings().femaleEvents = true;
        DataCache.getSettings().fatherSide = false;
        DataCache.getSettings().motherSide = true;

        Person currentUser = DataCache.getInstance().getCurrentPerson();
        List<Event> settingEvents = DataCache.getInstance().getEventsBySettings();
        Event earliestUserFather = DataCache.getInstance().getEarliestEventOfPerson(currentUser.getFatherID());
        assertFalse(settingEvents.contains(earliestUserFather));

        //Test for the current user and their spouse if they have one
        // Because only both of them should be there
        DataCache.getSettings().maleEvents = true;
        DataCache.getSettings().femaleEvents = true;
        DataCache.getSettings().fatherSide = false;
        DataCache.getSettings().motherSide = false;

        settingEvents = DataCache.getInstance().getEventsBySettings();
        Event earliestUser = DataCache.getInstance().getEarliestEventOfPerson(currentUser.getPersonID());
        Event earliestFather = DataCache.getInstance().getEarliestEventOfPerson(currentUser.getFatherID());
        Event earliestMother = DataCache.getInstance().getEarliestEventOfPerson(currentUser.getMotherID());

        assertTrue(settingEvents.contains(earliestUser));
        if (currentUser.getSpouseID() != null) {
            Event earliestUserSpouse = DataCache.getInstance().getEarliestEventOfPerson(currentUser.getSpouseID());
            assertTrue(settingEvents.contains(earliestUserSpouse));
        }
        assertFalse(settingEvents.contains(earliestFather));
        assertFalse(settingEvents.contains(earliestMother));

        //The current user won't show up if male events are turned off
        DataCache.getSettings().maleEvents = false;
        DataCache.getSettings().femaleEvents = true;
        DataCache.getSettings().fatherSide = true;
        DataCache.getSettings().motherSide = true;

        settingEvents = DataCache.getInstance().getEventsBySettings();
        assertFalse(settingEvents.contains(earliestUser));

        //The current user's father won't show up if father side is turned off
        DataCache.getSettings().maleEvents = true;
        DataCache.getSettings().femaleEvents = true;
        DataCache.getSettings().fatherSide = false;
        DataCache.getSettings().motherSide = true;

        settingEvents = DataCache.getInstance().getEventsBySettings();
        assertFalse(settingEvents.contains(earliestFather));

        //The current user's mother won't show up if mother side is turned off
        DataCache.getSettings().maleEvents = true;
        DataCache.getSettings().femaleEvents = true;
        DataCache.getSettings().fatherSide = true;
        DataCache.getSettings().motherSide = false;

        settingEvents = DataCache.getInstance().getEventsBySettings();
        assertFalse(settingEvents.contains(earliestMother));
    }

    @Test
    public void noEventsDisplayTest() {
        //This checks if the the HTTP response is a 200
        assertTrue(ServerProxy.postRegisterUser(SERVER_HOST, SERVER_PORT, testRegisterRequest));
        //For extra surety, check if the user logged in variable has been set to true
        assertTrue(DataCache.getInstance().userLoggedIn);

        //If these two events are both false then no events will be displayed
        DataCache.getSettings().maleEvents = false;
        DataCache.getSettings().femaleEvents = false;

        List<Event> eventsFromSettings = DataCache.getInstance().getEventsBySettings();
        assertNull(eventsFromSettings);
    }
}
