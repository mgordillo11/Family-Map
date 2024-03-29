package com.example.family_map;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Models.Authtoken;
import Models.Event;
import Models.Person;
import RequestResponse.EventResponse;
import RequestResponse.LoginRequest;
import RequestResponse.LoginResponse;
import RequestResponse.PersonResponse;
import RequestResponse.RegisterRequest;
import RequestResponse.RegisterResponse;

public class ServerProxy {
    public static boolean postRegisterUser(String serverHost, String serverPort, RegisterRequest registerRequest) {
        Gson gson = new Gson();
        boolean currentStatus;

        try {
            URL registerURL = new URL("http://" + serverHost + ":" + serverPort + "/user/register");

            HttpURLConnection http = (HttpURLConnection) registerURL.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true); //Only need this for POST requests

            http.addRequestProperty("Accept", "application/json");
            http.connect();

            OutputStream registerBody = http.getOutputStream();
            writeString(gson.toJson(registerRequest), registerBody);
            registerBody.close();

            if (http.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream successRespBody = http.getInputStream();
                String jsonResponse = readString(successRespBody);

                RegisterResponse registerResponse = gson.fromJson(jsonResponse, RegisterResponse.class);

                DataCache.getInstance().setCurrentAuthtoken(new Authtoken(registerResponse.getAuthtoken(),
                        registerResponse.getUsername()));

                currentStatus = getPeopleByUser(serverHost, serverPort,
                        DataCache.getInstance().getCurrentAuthtoken());

                if (!currentStatus) {
                    return false;
                }

                currentStatus = getEventsByUser(serverHost, serverPort,
                        DataCache.getInstance().getCurrentAuthtoken());

                //Populates both lists based on if the event was associated with a male or female
                for (Map.Entry<String, Event> recentLoadedEvent : DataCache.getInstance().getEvents().entrySet()) {
                    Person checkGenderEvent = DataCache.getInstance().getFamilyPersonTree()
                            .get(recentLoadedEvent.getValue().getPersonID());

                    if (checkGenderEvent.getGender().equals("m")) {
                        DataCache.getInstance().getMaleEvents().add(recentLoadedEvent.getValue());
                    } else {
                        DataCache.getInstance().getFemaleEvents().add(recentLoadedEvent.getValue());
                    }
                }

                DataCache.getInstance().setCurrentPerson(getPersonViaID(registerResponse.getPersonID()));

                //Populates the mother side of the current user
                Person currentUserMother = DataCache.getInstance().getFamilyPersonTree().
                        get(DataCache.getInstance().getCurrentPerson().getMotherID());

                Person currentUserFather = DataCache.getInstance().getFamilyPersonTree().
                        get(DataCache.getInstance().getCurrentPerson().getFatherID());

                //Populates the mother side of the current user
                DataCache.getInstance().fillMaternalAncestors(currentUserMother);

                //Populates the father side of the current user
                DataCache.getInstance().fillPaternalAncestors(currentUserFather);

                DataCache.getInstance().userLoggedIn = true;
                return currentStatus;
            } else {
                System.out.println("ERROR: " + http.getResponseMessage());
                // Get the error stream containing the HTTP response body (if any)
                InputStream respBody = http.getErrorStream();
                // Extract data from the HTTP response body
                String respData = readString(respBody);
                // Display the data returned from the server
                System.out.println(respData);
                DataCache.getInstance().userLoggedIn = false;
                return false;
            }

        } catch (IOException err) {
            err.printStackTrace();
            return false;
        }
    }

    public static boolean postLoginUser(String serverHost, String serverPort, LoginRequest loginRequest) {
        Gson gson = new Gson();
        boolean currentResponseStatus;

        try {
            URL loginURL = new URL("http://" + serverHost + ":" + serverPort + "/user/login");

            HttpURLConnection httpURLConnection = (HttpURLConnection) loginURL.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);

            httpURLConnection.addRequestProperty("Accept", "application/json");
            httpURLConnection.connect();

            OutputStream loginBody = httpURLConnection.getOutputStream();
            writeString(gson.toJson(loginRequest), loginBody);
            loginBody.close();

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream successLoginBody = httpURLConnection.getInputStream();
                String loginJson = readString(successLoginBody);

                LoginResponse loginResponse = gson.fromJson(loginJson, LoginResponse.class);

                DataCache.getInstance().setCurrentAuthtoken(new Authtoken(loginResponse.getAuthtoken(),
                        loginResponse.getUsername()));

                currentResponseStatus = getPeopleByUser(serverHost, serverPort,
                        DataCache.getInstance().getCurrentAuthtoken());

                if (!currentResponseStatus) {
                    return false;
                }
                currentResponseStatus = getEventsByUser(serverHost, serverPort,
                        DataCache.getInstance().getCurrentAuthtoken());

                for (Map.Entry<String, Event> recentLoadedEvent : DataCache.getInstance().getEvents().entrySet()) {
                    Person checkGenderEvent = DataCache.getInstance().getFamilyPersonTree()
                            .get(recentLoadedEvent.getValue().getPersonID());

                    if (checkGenderEvent.getGender().equals("m")) {
                        DataCache.getInstance().getMaleEvents().add(recentLoadedEvent.getValue());
                    } else {
                        DataCache.getInstance().getFemaleEvents().add(recentLoadedEvent.getValue());
                    }
                }

                DataCache.getInstance().setCurrentPerson(getPersonViaID(loginResponse.getPersonID()));

                //Populates the mother side of the current user
                Person currentUserMother = DataCache.getInstance().getFamilyPersonTree().
                        get(DataCache.getInstance().getCurrentPerson().getMotherID());

                Person currentUserFather = DataCache.getInstance().getFamilyPersonTree().
                        get(DataCache.getInstance().getCurrentPerson().getFatherID());

                //Populates the mother side of the current user
                DataCache.getInstance().fillMaternalAncestors(currentUserMother);
                //Populates the father side of the current user
                DataCache.getInstance().fillPaternalAncestors(currentUserFather);

                DataCache.getInstance().userLoggedIn = true;
                return currentResponseStatus;
            } else {
                System.out.println("ERROR: " + httpURLConnection.getResponseMessage());

                // Get the error stream containing the HTTP response body (if any)
                InputStream respBody = httpURLConnection.getErrorStream();

                // Extract data from the HTTP response body
                String respData = readString(respBody);

                // Display the data returned from the server
                System.out.println(respData);

                DataCache.getInstance().userLoggedIn = false;
                return false;
            }

        } catch (IOException err) {
            err.printStackTrace();
            return false;
        }
    }

    public static boolean getPeopleByUser(String serverHost, String serverPort, Authtoken userAuthtoken) {
        Gson gson = new Gson();

        try {
            URL peopleURL = new URL("http://" + serverHost + ":" + serverPort + "/person");

            HttpURLConnection httpURLConnection = (HttpURLConnection) peopleURL.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoOutput(false);

            httpURLConnection.addRequestProperty("Authorization", userAuthtoken.getAuthtoken());
            httpURLConnection.addRequestProperty("Accept", "application/json");

            httpURLConnection.connect();

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream responseStream = httpURLConnection.getInputStream();
                String peopleJson = readString(responseStream);

                PersonResponse personResponse = gson.fromJson(peopleJson, PersonResponse.class);

                for (Person currentPerson : personResponse.getData()) {
                    DataCache.getInstance().getFamilyPersonTree().put(currentPerson.getPersonID(), currentPerson);
                }

                return true;
            } else {
                System.out.println("ERROR: " + httpURLConnection.getResponseMessage());
                // Get the error stream containing the HTTP response body (if any)
                InputStream respBody = httpURLConnection.getErrorStream();

                // Extract data from the HTTP response body
                String respData = readString(respBody);

                // Display the data returned from the server
                System.out.println(respData);
                return false;
            }

        } catch (IOException err) {
            err.printStackTrace();
            return false;
        }
    }

    public static boolean getEventsByUser(String serverHost, String serverPort, Authtoken userAuthtoken) {
        Gson gson = new Gson();

        try {
            URL eventsURL = new URL("http://" + serverHost + ":" + serverPort + "/event");

            HttpURLConnection httpURLConnection = (HttpURLConnection) eventsURL.openConnection();

            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoOutput(false);
            httpURLConnection.addRequestProperty("Authorization", userAuthtoken.getAuthtoken());

            httpURLConnection.addRequestProperty("Accept", "application/json");
            httpURLConnection.connect();

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream successRespBody = httpURLConnection.getInputStream();
                String jsonResponse = readString(successRespBody);

                EventResponse eventResponse = gson.fromJson(jsonResponse, EventResponse.class);

                for (Event currentEvent : eventResponse.getData()) {
                    DataCache.getInstance().getEvents().put(currentEvent.getEventID(), currentEvent);
                    if (!(DataCache.getInstance().getEventsOfPerson().containsKey(currentEvent.getPersonID()))) {
                        List<Event> userEvents = new ArrayList<>();
                        DataCache.getInstance().getEventsOfPerson().put(currentEvent.getPersonID(), userEvents);
                    }
                    DataCache.getInstance().getEventsOfPerson().get(currentEvent.getPersonID()).add(currentEvent);
                }
                return true;
            } else {
                System.out.println("ERROR: " + httpURLConnection.getResponseMessage());
                // Get the error stream containing the HTTP response body (if any)
                InputStream respBody = httpURLConnection.getErrorStream();
                // Extract data from the HTTP response body
                String respData = readString(respBody);
                // Display the data returned from the server
                System.out.println(respData);
                return false;
            }

        } catch (IOException err) {
            err.printStackTrace();
            return false;
        }
    }

    /*
    The readString method shows how to read a String from an InputStream.
*/
    private static String readString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader sr = new InputStreamReader(is);
        char[] buf = new char[1024];
        int len;
        while ((len = sr.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }
        return sb.toString();
    }

    /*
        The writeString method shows how to write a String to an OutputStream.
    */
    private static void writeString(String str, OutputStream os) throws IOException {
        OutputStreamWriter sw = new OutputStreamWriter(os);
        sw.write(str);
        sw.flush();
    }

    //Function that simple finds a Person by their ID
    private static Person getPersonViaID(String personID) {
        return DataCache.getInstance().getFamilyPersonTree().get(personID);
    }

}
