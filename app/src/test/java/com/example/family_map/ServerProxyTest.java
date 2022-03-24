package com.example.family_map;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import RequestResponse.LoginRequest;
import RequestResponse.RegisterRequest;

public class ServerProxyTest {

    @BeforeEach
    public void setup() {

    }

    @Test
    public void registerTaskPositive() {
        RegisterRequest positiveRegisterRequest = new RegisterRequest("Manny", "Password",
                "fake@email.com", "Manuel", "Gordillo", "m");

        //This checks if the the HTTP response is a 200
        assertTrue(ServerProxy.postRegisterUser("localhost", "8080", positiveRegisterRequest));
        //For extra surety, check if the user logged in variable has been set to true
        System.out.println(DataCache.getInstance().userLoggedIn);
        assertTrue(DataCache.getInstance().userLoggedIn);
    }

    @Test
    public void registerTaskNegative() {
        RegisterRequest repeatedRegisterRequest = new RegisterRequest("MannyBoi", "Password123",
                "fake@email.com", "Manuel", "Gordillo", "m");
        //This checks if the the HTTP response is a 200
        assertTrue(ServerProxy.postRegisterUser("localhost", "8080", repeatedRegisterRequest));

        //This checks if the the HTTP response is a 400, this is because there can't be more than one same user
        assertFalse(ServerProxy.postRegisterUser("localhost", "8080", repeatedRegisterRequest));
        //For extra surety, check if the user logged in variable has been set to false
        assertFalse(DataCache.getInstance().userLoggedIn);

    }

    @Test
    public void peopleRelatedToUserPositive() {
        RegisterRequest positiveForPeopleRegister = new RegisterRequest("Gemini", "FakePassword",
                "fake@email.com", "GMan", "Big Man", "f");

        //This checks if the the HTTP response is a 200
        assertTrue(ServerProxy.postRegisterUser("localhost", "8080", positiveForPeopleRegister));
    }

    @Test
    public void peopleRelatedToUserNegative() {

    }

    @Test
    public void loginTaskPositive() {
        RegisterRequest loginPositiveRegister = new RegisterRequest("MannyFakeBoi", "Password997",
                "fake@email.com", "Manuel", "Gordillo", "m");

        //This checks if the the HTTP response is a 200
        assertTrue(ServerProxy.postRegisterUser("localhost", "8080", loginPositiveRegister));
        //For extra surety, check if the user logged in variable has been set to true
        // This can only be set to true if any errors were thrown by the end of the api call
        assertTrue(DataCache.getInstance().userLoggedIn);

        LoginRequest loginPositiveRequest = new LoginRequest("MannyFakeBoi", "Password997");

        //If no errors occurred then a 200 request will be returned in the form of a true boolean
        assertTrue(ServerProxy.postLoginUser("localhost", "8080", loginPositiveRequest));
        //Checks if user logged in variable has been set to true
        assertTrue(DataCache.getInstance().userLoggedIn);
    }

    @Test
    public void loginTaskNegative() {
        LoginRequest invalidLoginRequest = new LoginRequest("Fake", "Password");

        //Checks for a 400 Bad Request
        assertFalse(ServerProxy.postLoginUser("localhost", "8080", invalidLoginRequest));

        //Checks if the user logged in variable hasn't been set to true
        assertFalse(DataCache.getInstance().userLoggedIn);
    }
}
