package com.example.family_map;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import Models.Authtoken;
import Models.Event;
import Models.Person;

public class DataCache {
    //APPROACH #2 - Singleton Pattern
    private static DataCache instance = new DataCache();

    public static DataCache getInstance() {
        return instance;
    }

    private DataCache() {
        familyPeople = new HashMap<>();
        familyEvents = new HashMap<>();
        personEvents = new HashMap<>();
        userFamily = new HashMap<>();
        paternalAncestors = new HashSet<>();
        maternalAncestors = new HashSet<>();
    }

    private Map<String, Person> familyPeople; //Get's a person based on their personID
    private Map<String, Event> familyEvents; //Get an event based on their event ID

    private Map<String, List<Event>> personEvents; //Get a list of events chronologically based on a person's ID
    private Map<String, List<Person>> userFamily;

    private Set<String> paternalAncestors; //Get ancestors on dad's side
    private Set<String> maternalAncestors; //Get ancestor's on mom's side

    private Authtoken currentAuthtoken;
    private Person currentPerson;

    public Authtoken getCurrentAuthtoken() {
        return currentAuthtoken;
    }

    public void setCurrentAuthtoken(Authtoken currentAuthtoken) {
        this.currentAuthtoken = currentAuthtoken;
    }

    public Map<String, List<Event>> getPersonEvents() {
        return personEvents;
    }

    public Map<String, List<Person>> getUserFamily() {
        return userFamily;
    }

    public Map<String, Event> getEvents() {
        return familyEvents;
    }

    public Person getCurrentPerson() {
        return currentPerson;
    }

    public void setCurrentPerson(Person currentPerson) {
        this.currentPerson = currentPerson;
    }

    public Map<String, Person> getFamilyPeople() {
        return familyPeople;
    }

    public void setFamilyPeople(Map<String, Person> familyPeople) {
        this.familyPeople = familyPeople;
    }
// Settings settings  (Class for future assignments)

    //APPROACH #1 - STATIC CLASS
    //static Map<String personID, Person> people;
    //static Map<String eventID, Event> events;
    //static List<Event> getEventsForPerson(personID) {}
}
