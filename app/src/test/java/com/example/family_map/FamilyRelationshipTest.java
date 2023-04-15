package com.example.family_map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import Models.Person;
import RequestResponse.LoginRequest;

public class FamilyRelationshipTest {
    //This will only work with Sheila Parker Data
    //So make sure to LOAD the Json Data
    private final String SERVER_HOST = "localhost";
    private final String SERVER_PORT = "8080";
    private LoginRequest loginPositiveRequest;

    @Before
    public void setup() {
        DataCache.getInstance().resetCacheData();
        loginPositiveRequest = new LoginRequest("sheila",
                "parker");
    }

    @Test
    public void familyRelationshipPositive() {
        loginPositiveRequest = new LoginRequest("sheila",
                "parker");

        //If no errors occurred then a 200 request will be returned in the form of a true boolean
        assertTrue(ServerProxy.postLoginUser(SERVER_HOST, SERVER_PORT, loginPositiveRequest));
        //Checks if user logged in variable has been set to true
        assertTrue(DataCache.getInstance().userLoggedIn);

        Person currentUser = DataCache.getInstance().getCurrentPerson();
        Person currentUserFather = DataCache.getInstance().getFamilyPersonTree().get(currentUser.getFatherID());
        Person currentUserMother = DataCache.getInstance().getFamilyPersonTree().get(currentUser.getMotherID());

        assertEquals(currentUser.getMotherID(), "Betty_White");
        assertEquals(currentUser.getFatherID(), "Blaine_McGary");
        assertEquals(currentUser.getSpouseID(), "Davis_Hyer");
        assertNull(DataCache.getInstance().getChildFromParent(currentUser.getPersonID()));

        //This checks that sheila is a a child of the father ID that is associated with her
        // and also the Mother ID
        assertNotNull(DataCache.getInstance().getChildFromParent(currentUserFather.getPersonID()));
        assertNotNull(DataCache.getInstance().getChildFromParent(currentUserMother.getPersonID()));
    }

    @Test
    public void familyRelationshipNegative() {
        loginPositiveRequest = new LoginRequest("sheila",
                "parker");

        //If no errors occurred then a 200 request will be returned in the form of a true boolean
        assertTrue(ServerProxy.postLoginUser(SERVER_HOST, SERVER_PORT, loginPositiveRequest));
        //Checks if user logged in variable has been set to true
        assertTrue(DataCache.getInstance().userLoggedIn);

        //Tests another user's person ID which should return NUll since he/she should not be in the DataCache
        assertNull(DataCache.getInstance().getFamilyPersonTree().get("Patrick_Spencer"));
    }
}

