package com.example.family_map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.Random;
import java.util.UUID;

import Models.Person;
import RequestResponse.RegisterRequest;

public class SearchTest {
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
    public void searchFilterManyResults() {
        //This checks if the the HTTP response is a 200
        assertTrue(ServerProxy.postRegisterUser(SERVER_HOST, SERVER_PORT, testRegisterRequest));
        //For extra surety, check if the user logged in variable has been set to true
        assertTrue(DataCache.getInstance().userLoggedIn);

        Random random = new Random();
        String randomPersonID = DataCache.getInstance().getFamilyPersonTree().keySet()
                .stream().skip(random.nextInt(DataCache.getInstance().getFamilyPersonTree().size())).findFirst().orElse(null);

        Person randomPerson = DataCache.getInstance().getFamilyPersonTree().get(randomPersonID);
        DataCache.getInstance().searchFilter(randomPerson.getFirstName());

        if (DataCache.getInstance().testingEvents.size() == 1
                && randomPersonID.equals(DataCache.getInstance().getCurrentPerson().getPersonID())) {
            assertEquals(DataCache.getInstance().testingEvents.size(), 1);
            assertEquals(DataCache.getInstance().testingPeople.size(), 1);
        } else {
            assertTrue(DataCache.getInstance().testingEvents.size() >= 3);
            assertTrue(DataCache.getInstance().testingPeople.size() >= 1);
        }
    }

    @Test
    public void searchFilterNoResults() {
        //This checks if the the HTTP response is a 200
        assertTrue(ServerProxy.postRegisterUser(SERVER_HOST, SERVER_PORT, testRegisterRequest));
        //For extra surety, check if the user logged in variable has been set to true
        assertTrue(DataCache.getInstance().userLoggedIn);

        String invalidSearch = "yee buddy this random search should be random enough, 28th/03/2020";
        DataCache.getInstance().searchFilter(invalidSearch);

        assertEquals(0, DataCache.getInstance().testingEvents.size());
        assertEquals(0, DataCache.getInstance().testingPeople.size());
    }
}
