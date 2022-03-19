package com.example.family_map;

import static org.junit.Assert.*;
import org.junit.Test;

import RequestResponse.LoginRequest;
import RequestResponse.RegisterRequest;

public class ServerProxyTest {
    @Test
    public void registerTaskPositive() {

    }

    @Test
    public void registerTaskNegative() {
        RegisterRequest originalRegisterRequest = new RegisterRequest("Manny", "Password",
                "fake@email.com", "Manuel", "Gordillo", "m");

        //This
        assertTrue(ServerProxy.postRegisterUser("localhost", "8080", originalRegisterRequest));



    }

    @Test
    public void loginTaskPositive() {

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
